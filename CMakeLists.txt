# CMakeLists.txt
cmake_minimum_required(VERSION 3.10)
project(lab3-beta VERSION 0.4 LANGUAGES Java)

include("${CMAKE_SOURCE_DIR}/variables.cmake")

find_program(FFPLAY_EXECUTABLE
    NAMES ffplay ffplay.exe
    PATHS "${CMAKE_SOURCE_DIR}/scripts" ENV PATH
)

add_custom_target(do_clean
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: clean] Removing build directories..."
  COMMAND ${CMAKE_COMMAND} -E remove_directory "${CLASSES_DIR}"
  COMMAND ${CMAKE_COMMAND} -E remove_directory "${TEST_CLASSES_DIR}"
  COMMAND ${CMAKE_COMMAND} -E remove_directory "${DIST_DIR}"
  COMMAND ${CMAKE_COMMAND} -E remove_directory "${BUILD_DIR}/test-results"
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: clean] Done."
)

add_custom_target(ant_prebuild
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: ant] Running native2ascii and doc..."
  COMMAND ant native2ascii doc
  WORKING_DIRECTORY "${CMAKE_SOURCE_DIR}"
)

add_custom_target(compile
  DEPENDS ant_prebuild
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: compile] Compiling main sources..."
  COMMAND ${CMAKE_COMMAND} -E make_directory "${CLASSES_DIR}"
  COMMAND javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${CLASSPATH}" ${JAVA_SOURCES}
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: compile] Completed."
)

add_custom_target(generate_manifest
  COMMAND ${CMAKE_COMMAND} -E make_directory "${CMAKE_SOURCE_DIR}/build"
  COMMAND ${CMAKE_COMMAND} -E echo "Manifest-Version: 1.0"
    > "${CMAKE_SOURCE_DIR}/build/manifest.txt"
  COMMAND ${CMAKE_COMMAND} -E echo "Implementation-Version: ${PROJECT_VERSION}"
    >> "${CMAKE_SOURCE_DIR}/build/manifest.txt"
)


# ─── stage: build ───────────────────────────────────────────────────────────────────
add_custom_target(build
  DEPENDS compile generate_manifest
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: build] Packaging JAR..."
  COMMAND ${CMAKE_COMMAND} -E make_directory "${DIST_DIR}"
  COMMAND "${JAR_EXECUTABLE}" cfm "${DIST_DIR}/${PROJECT_NAME}-${PROJECT_VERSION}.jar"
    "${CMAKE_SOURCE_DIR}/build/manifest.txt" -C "${CLASSES_DIR}" .
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: build] JAR created at dist/${PROJECT_NAME}-${PROJECT_VERSION}.jar"
)

# ─── stage: music ───────────────────────────────────────────────────────────────────
add_custom_target(music
  DEPENDS build
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: build-music] Playing music..."
  COMMAND ${CMAKE_COMMAND} -E cmake_echo_color --magenta
    $<$<BOOL:${FFPLAY_EXECUTABLE}>:"[Stage: build-music] Running ffplay":"[Stage: build-music] Skipping music: ffplay not found">
  COMMAND
    $<$<BOOL:${FFPLAY_EXECUTABLE}>:${FFPLAY_EXECUTABLE}>
    $<$<BOOL:${FFPLAY_EXECUTABLE}>:-nodisp>
    $<$<BOOL:${FFPLAY_EXECUTABLE}>:-autoexit>
    $<$<BOOL:${FFPLAY_EXECUTABLE}>:${CMAKE_SOURCE_DIR}/resources/divine-melody.wav>
)


add_custom_target(compile-tests
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: compile-tests] Compiling test sources..."
  COMMAND ${CMAKE_COMMAND} -E make_directory "${TEST_CLASSES_DIR}"
  COMMAND javac -encoding UTF-8 -d "${TEST_CLASSES_DIR}" -cp "${CLASSPATH}${CP_SEP}${CLASSES_DIR}" ${TEST_SOURCES}
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: compile-tests] Completed."
)

add_custom_target(test
  DEPENDS compile-tests
  WORKING_DIRECTORY "${CMAKE_SOURCE_DIR}"
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: test] Running JUnit tests via Ant..."
  COMMAND ant test
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: test] Ant test target finished."
)

add_custom_target(report
  DEPENDS test
  WORKING_DIRECTORY "${CMAKE_SOURCE_DIR}"
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: report] Committing XML reports via Ant..."
  COMMAND ant report
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: report] Ant report target finished."
)

add_custom_target(scp
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: scp] Removing remote JAR..."
  COMMAND ssh -p ${SCP_PORT} ${SCP_USER}@${SCP_HOST} "rm -rf ${SCP_REMOTE_DIR}/${PROJECT_NAME}-${PROJECT_VERSION}.jar"
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: scp] Uploading new JAR..."
  COMMAND scp -P ${SCP_PORT} "${DIST_DIR}/${PROJECT_NAME}-${PROJECT_VERSION}.jar" ${SCP_USER}@${SCP_HOST}:${SCP_REMOTE_DIR}
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: scp] Done."
)

add_custom_target(diff
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: diff] Checking/creating SVN repo..."
  COMMAND cmd /C if not exist "${CMAKE_SOURCE_DIR}\\svnrepo" svnadmin create "${CMAKE_SOURCE_DIR}\\svnrepo"

  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: diff] Ensuring SVN trunk exists..."
  COMMAND cmd /C svn list "file:///${CMAKE_SOURCE_DIR}/svnrepo/trunk" >nul 2>nul || svn import "${SRC_DIR}" "file:///${CMAKE_SOURCE_DIR}/svnrepo/trunk" -m "Initial import"

  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: diff] Resetting working copy..."
  COMMAND ${CMAKE_COMMAND} -E remove_directory "${SRC_DIR}"
  COMMAND ${CMAKE_COMMAND} -E make_directory "${SRC_DIR}"

  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: diff] Checking out from SVN..."
  COMMAND cmd /C svn checkout "file:///${CMAKE_SOURCE_DIR}/svnrepo/trunk" "${SRC_DIR}"

  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: diff] Committing changes (if any)..."
  COMMAND cmd /C svn commit "${SRC_DIR}" -m "Auto-commit by CMake: updating modified Java classes" --quiet || echo [Stage: diff] No changes to commit.
)

add_custom_target(history
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: history] Attempting compile; if it fails, rollback via Git..."
  COMMAND sh -c "cmake --build \"${CMAKE_BINARY_DIR}\" --target compile || true"
  COMMAND sh -c "if [ ! -d \"${CLASSES_DIR}\" ] && git rev-parse --is-inside-work-tree &> /dev/null; then \
                    ${CMAKE_COMMAND} -E echo \"[Stage: history] Compile failed; checking out previous Git revision...\" && \
                    git checkout HEAD~1 && \
                    cmake --build \"${CMAKE_BINARY_DIR}\" --target history; \
                 else \
                    echo \"[Stage: history] Compile succeeded or Git not present; no rollback.\"; \
                 fi"
)

add_custom_target(env
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: env] Reading alternative environments..."
  COMMAND ${CMAKE_COMMAND} -E remove_directory "${BUILD_DIR}/env-classes"
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: env] Compiling under alternative env..."
  COMMAND ${CMAKE_COMMAND} -E make_directory "${BUILD_DIR}/env-classes"
  COMMAND javac -encoding UTF-8 -d "${BUILD_DIR}/env-classes" -cp "${CLASSPATH}" ${JAVA_SOURCES}
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: env] Compiled to build/env-classes"
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: env] Checking Java version..."
  COMMAND java -version
)