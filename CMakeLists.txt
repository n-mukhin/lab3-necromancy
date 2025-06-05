cmake_minimum_required(VERSION 3.10)
project(lab3-beta VERSION 0.4 LANGUAGES Java)

set(SRC_DIR            "${CMAKE_SOURCE_DIR}/src/main/java")
set(TEST_SRC_DIR       "${CMAKE_SOURCE_DIR}/src/test/java")
set(BUILD_DIR          "${CMAKE_SOURCE_DIR}/build")
set(CLASSES_DIR        "${BUILD_DIR}/classes")
set(TEST_CLASSES_DIR   "${BUILD_DIR}/test-classes")
set(DIST_DIR           "${CMAKE_SOURCE_DIR}/dist")
set(LIB_DIR            "${CMAKE_SOURCE_DIR}/lib")

file(GLOB_RECURSE JAVA_SOURCES   "${SRC_DIR}/*.java")
file(GLOB_RECURSE TEST_SOURCES   "${TEST_SRC_DIR}/*.java")
file(GLOB        LIB_JARS        "${LIB_DIR}/*.jar")

if(WIN32)
  set(CP_SEP ";")
else()
  set(CP_SEP ":")
endif()
list(JOIN LIB_JARS "${CP_SEP}" CLASSPATH)

find_program(JAR_EXECUTABLE
  NAMES jar.exe jar
  HINTS ENV JAVA_HOME PATH_SUFFIXES bin
)
if(NOT JAR_EXECUTABLE)
  message(FATAL_ERROR "jar not found")
endif()

# ─── clean ──────────────────────────────────────────────────────────────────────
add_custom_target(do_clean
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: clean] Removing build directories..."
  COMMAND ${CMAKE_COMMAND} -E remove_directory "${CLASSES_DIR}"
  COMMAND ${CMAKE_COMMAND} -E remove_directory "${TEST_CLASSES_DIR}"
  COMMAND ${CMAKE_COMMAND} -E remove_directory "${DIST_DIR}"
  COMMAND ${CMAKE_COMMAND} -E remove_directory "${BUILD_DIR}/test-results"
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: clean] Done."
)

# ─── compile main sources ───────────────────────────────────────────────────────
add_custom_target(compile
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: compile] Compiling main sources..."
  COMMAND ${CMAKE_COMMAND} -E make_directory "${CLASSES_DIR}"
  COMMAND javac -encoding UTF-8 -d "${CLASSES_DIR}" -cp "${CLASSPATH}" ${JAVA_SOURCES}
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: compile] Completed."
)

# ─── build JAR ─────────────────────────────────────────────────────────────────
add_custom_target(build
  DEPENDS compile
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: build] Packaging JAR..."
  COMMAND ${CMAKE_COMMAND} -E make_directory "${DIST_DIR}"
  COMMAND "${JAR_EXECUTABLE}" cfm "${DIST_DIR}/${PROJECT_NAME}-${PROJECT_VERSION}.jar" "${CMAKE_SOURCE_DIR}/build/manifest.txt" -C "${CLASSES_DIR}" .
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: build] JAR created at dist/${PROJECT_NAME}-${PROJECT_VERSION}.jar"
)

# ─── compile tests ──────────────────────────────────────────────────────────────
add_custom_target(compile-tests
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: compile-tests] Compiling test sources..."
  COMMAND ${CMAKE_COMMAND} -E make_directory "${TEST_CLASSES_DIR}"
  COMMAND javac -encoding UTF-8 -d "${TEST_CLASSES_DIR}" -cp "${CLASSPATH}${CP_SEP}${CLASSES_DIR}" ${TEST_SOURCES}
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: compile-tests] Completed."
)

# ─── run tests via Ant (unchanged) ───────────────────────────────────────────────
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

# ─── SCP ────────────────────────────────────────────────────────────────────────
set(SCP_PORT 2222)                     # override as needed
set(SCP_USER "s409203")            # override as needed
set(SCP_HOST "se.ifmo.ru")            # override as needed
set(SCP_REMOTE_DIR "Jar")   # override as needed

add_custom_target(scp
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: scp] Removing remote JAR..."
  COMMAND ssh -p ${SCP_PORT} ${SCP_USER}@${SCP_HOST} "rm -rf ${SCP_REMOTE_DIR}/${PROJECT_NAME}-${PROJECT_VERSION}.jar"
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: scp] Uploading new JAR..."
  COMMAND scp -P ${SCP_PORT} "${DIST_DIR}/${PROJECT_NAME}-${PROJECT_VERSION}.jar" ${SCP_USER}@${SCP_HOST}:${SCP_REMOTE_DIR}
  COMMAND ${CMAKE_COMMAND} -E echo "[Stage: scp] Done."
)

# ─── diff (simplified) ──────────────────────────────────────────────────────────
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

# ─── history ────────────────────────────────────────────────────────────────────
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

# ─── env ────────────────────────────────────────────────────────────────────────
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
