# CMakeLists.txt
cmake_minimum_required(VERSION 3.10)
project(lab3-beta VERSION 0.4 LANGUAGES Java)

include("${CMAKE_SOURCE_DIR}/variables.cmake")

find_program(FFPLAY_EXECUTABLE
    NAMES ffplay ffplay.exe
    PATHS "${CMAKE_SOURCE_DIR}/scripts" ENV PATH
)

# do_clean
add_custom_target(do_clean
  COMMAND ${CMAKE_COMMAND} -E echo "[do_clean] Removing build dirs"
  COMMAND ${CMAKE_COMMAND} -E remove_directory "${CLASSES_DIR}"
  COMMAND ${CMAKE_COMMAND} -E remove_directory "${TEST_CLASSES_DIR}"
  COMMAND ${CMAKE_COMMAND} -E remove_directory "${DIST_DIR}"
  COMMAND ${CMAKE_COMMAND} -E remove_directory "${BUILD_DIR}/test-results"
  COMMAND ${CMAKE_COMMAND} -E echo "[do_clean] Done"
)

# native2ascii
add_custom_target(native2ascii
  COMMAND ${CMAKE_COMMAND} -E echo "[native2ascii] Converting resource bundles"
  COMMAND ant native2ascii
  WORKING_DIRECTORY "${CMAKE_SOURCE_DIR}"
)

# doc
add_custom_target(doc
  COMMAND ${CMAKE_COMMAND} -E echo "[doc] Generating manifest and javadoc"
  COMMAND ant doc
  WORKING_DIRECTORY "${CMAKE_SOURCE_DIR}"
)

# compile
add_custom_target(compile
  DEPENDS native2ascii
  COMMAND ${CMAKE_COMMAND} -E echo "[compile] Compiling Java sources"
  COMMAND ${CMAKE_COMMAND} -E make_directory "${CLASSES_DIR}"
  COMMAND javac -J-Xms128m -J-Xmx512m -encoding UTF-8 -d "${CLASSES_DIR}"
        -cp "${CLASSPATH}" ${JAVA_SOURCES}
  COMMAND ${CMAKE_COMMAND} -E echo "[compile] Done"
)

# generate_manifest
add_custom_target(generate_manifest
  DEPENDS compile
  COMMAND ${CMAKE_COMMAND} -E make_directory "${CMAKE_SOURCE_DIR}/build"
  COMMAND ${CMAKE_COMMAND} -E echo "Manifest-Version: 1.0"
    > "${CMAKE_SOURCE_DIR}/build/manifest.txt"
  COMMAND ${CMAKE_COMMAND} -E echo "Implementation-Version: ${PROJECT_VERSION}"
    >> "${CMAKE_SOURCE_DIR}/build/manifest.txt"
)

# build
add_custom_target(build
  DEPENDS compile generate_manifest
  COMMAND ${CMAKE_COMMAND} -E echo "[build] Packaging JAR"
  COMMAND ${CMAKE_COMMAND} -E make_directory "${DIST_DIR}"
  COMMAND "${JAR_EXECUTABLE}" -J-Xmx512m cfm
    "${DIST_DIR}/${PROJECT_NAME}-${PROJECT_VERSION}.jar"
    "${CMAKE_SOURCE_DIR}/build/manifest.txt"
    -C "${CLASSES_DIR}" .
  COMMAND ${CMAKE_COMMAND} -E echo "[build] Packaging WAR"
  COMMAND ${CMAKE_COMMAND} -E make_directory "${DIST_DIR}/war-staging"
  COMMAND ${CMAKE_COMMAND} -E copy_directory
    "${WEBAPP_DIR}" "${DIST_DIR}/war-staging"
  COMMAND ${CMAKE_COMMAND} -E copy_directory
    "${CLASSES_DIR}" "${DIST_DIR}/war-staging/WEB-INF/classes"
  COMMAND "${JAR_EXECUTABLE}" -J-Xmx512m cf
    "${DIST_DIR}/${PROJECT_NAME}-${PROJECT_VERSION}.war"
    -C "${DIST_DIR}/war-staging" .
  COMMAND ${CMAKE_COMMAND} -E echo "[build] Artifacts in ${DIST_DIR}"
)


# test
add_custom_target(test
  DEPENDS build
  WORKING_DIRECTORY "${CMAKE_SOURCE_DIR}"
  COMMAND ${CMAKE_COMMAND} -E echo "[test] Running JUnit via Ant"
  COMMAND ant test
  COMMAND ${CMAKE_COMMAND} -E echo "[test] Tests complete"
)

# report
add_custom_target(generate_report
  DEPENDS test
  WORKING_DIRECTORY "${CMAKE_SOURCE_DIR}"
  COMMAND ${CMAKE_COMMAND} -E echo "[report] Generating JUnit XML reports via Ant"
  COMMAND ant report
)

add_custom_target(report
  DEPENDS generate_report
  COMMAND ${CMAKE_COMMAND} -E echo "[commit_reports] Git status of XML reports"
  COMMAND ${CMAKE_COMMAND} -E chdir "${CMAKE_SOURCE_DIR}" git status -s ${BUILD_DIR}/test-results/*.xml
  COMMAND ${CMAKE_COMMAND} -E echo "[commit_reports] Forcibly staging XML reports"
  COMMAND ${CMAKE_COMMAND} -E chdir "${CMAKE_SOURCE_DIR}" git add -f ${BUILD_DIR}/test-results/*.xml
  COMMAND ${CMAKE_COMMAND} -E echo "[commit_reports] Diff of staged changes"
  COMMAND ${CMAKE_COMMAND} -E chdir "${CMAKE_SOURCE_DIR}" git --no-pager diff --staged --stat
  COMMAND ${CMAKE_COMMAND} -E echo "[commit_reports] Committing XML reports"
  COMMAND ${CMAKE_COMMAND} -E chdir "${CMAKE_SOURCE_DIR}" git commit -m "chore: add JUnit XML reports"
  COMMAND ${CMAKE_COMMAND} -E echo "[commit_reports] Latest commit details"
  COMMAND ${CMAKE_COMMAND} -E chdir "${CMAKE_SOURCE_DIR}" git log -1 --stat
)

# music
add_custom_target(music
  DEPENDS build
  COMMAND ${CMAKE_COMMAND} -E echo "[music] Playing build soundtrack"
  COMMAND ${CMAKE_COMMAND} -E cmake_echo_color --magenta
    $<$<BOOL:${FFPLAY_EXECUTABLE}>:"[music] ffplay found":"[music] ffplay missing, skipping">
  COMMAND
    $<$<BOOL:${FFPLAY_EXECUTABLE}>:${FFPLAY_EXECUTABLE}>
    $<$<BOOL:${FFPLAY_EXECUTABLE}>:-nodisp>
    $<$<BOOL:${FFPLAY_EXECUTABLE}>:-autoexit>
    $<$<BOOL:${FFPLAY_EXECUTABLE}>:${CMAKE_SOURCE_DIR}/resources/divine-melody.wav>
)

# scp
add_custom_target(scp
  DEPENDS build
  COMMAND ${CMAKE_COMMAND} -E echo "[scp] Uploading JAR & WAR"
  COMMAND ssh -p ${SCP_PORT} ${SCP_USER}@${SCP_HOST}
    rm -f ${SCP_REMOTE_DIR}/${PROJECT_NAME}-${PROJECT_VERSION}.jar
  COMMAND scp -P ${SCP_PORT}
    "${DIST_DIR}/${PROJECT_NAME}-${PROJECT_VERSION}.jar"
    ${SCP_USER}@${SCP_HOST}:${SCP_REMOTE_DIR}
  COMMAND ssh -p ${SCP_PORT} ${SCP_USER}@${SCP_HOST}
    rm -f ${SCP_REMOTE_WAR_DIR}/${PROJECT_NAME}-${PROJECT_VERSION}.war
  COMMAND scp -P ${SCP_PORT}
    "${DIST_DIR}/${PROJECT_NAME}-${PROJECT_VERSION}.war"
    ${SCP_USER}@${SCP_HOST}:${SCP_REMOTE_WAR_DIR}
  COMMAND ${CMAKE_COMMAND} -E echo "[scp] Done"
)



# diff
find_program(SVNADMIN_EXECUTABLE svnadmin)
if(NOT SVNADMIN_EXECUTABLE)
  message(FATAL_ERROR "svnadmin not found in PATH")
endif()

if(NOT EXISTS "${SVN_REPO_DIR}/format")
  execute_process(
    COMMAND ${SVNADMIN_EXECUTABLE} create "${SVN_REPO_DIR}"
    RESULT_VARIABLE _svn_rv
  )
  if(NOT _svn_rv EQUAL 0)
    message(FATAL_ERROR "Failed to create SVN repo")
  endif()
endif()

if(WIN32)
  add_custom_target(diff
    WORKING_DIRECTORY "${CMAKE_BINARY_DIR}"
    COMMAND cmd /C "if not exist \"${CLASSES_OUTPUT_DIR}\" (echo No diffs: classes directory not present.& exit /b 0)"
    COMMAND ${CMAKE_COMMAND} -E make_directory "${SVN_REPO_DIR}"
    COMMAND cmd /C "if not exist \"${SVN_REPO_DIR}\\format\" svnadmin create \"${SVN_REPO_DIR}\""
    COMMAND cmd /C "svn mkdir --parents ${SVN_REPO_URL}/trunk/classes -m auto 2>nul || echo path exists"
    COMMAND ${CMAKE_COMMAND} -E remove_directory "${SVN_WC_DIR}"
    COMMAND ${CMAKE_COMMAND} -E make_directory "${SVN_WC_DIR}"
    COMMAND cmd /C "svn checkout ${SVN_REPO_URL}/trunk/classes . 2>nul || svn update ."
    COMMAND ${CMAKE_COMMAND} -E remove_directory "${SVN_WC_DIR}/classes"
    COMMAND ${CMAKE_COMMAND} -E copy_directory "${CLASSES_OUTPUT_DIR}" "${SVN_WC_DIR}/classes"
    COMMAND cmd /C "svn add --force classes 2>nul || echo already versioned"
    COMMAND cmd /C "svn commit classes -m auto 2>nul || echo No changes to commit"
    COMMAND cmd /C "svn diff classes || echo No diffs"
  )
else()
  add_custom_target(diff
    COMMAND ${CMAKE_COMMAND} -E make_directory "${SVN_REPO_DIR}"

    COMMAND - svn mkdir --parents "${SVN_REPO_URL}/trunk/classes" -m auto

    COMMAND ${CMAKE_COMMAND} -E remove_directory "${SVN_WC_DIR}"
    COMMAND ${CMAKE_COMMAND} -E make_directory "${SVN_WC_DIR}"

    COMMAND - svn checkout "${SVN_REPO_URL}/trunk/classes" "${SVN_WC_DIR}"
    COMMAND - svn update "${SVN_WC_DIR}"

    COMMAND ${CMAKE_COMMAND} -E remove_directory "${SVN_WC_DIR}/classes"
    COMMAND ${CMAKE_COMMAND} -E copy_directory "${CLASSES_OUTPUT_DIR}" "${SVN_WC_DIR}/classes"

    COMMAND ${CMAKE_COMMAND} -E chdir "${SVN_WC_DIR}" svn add --force classes
    COMMAND ${CMAKE_COMMAND} -E chdir "${SVN_WC_DIR}" svn commit -m auto classes

    COMMAND ${CMAKE_COMMAND} -E chdir "${SVN_WC_DIR}" svn diff classes
  )
endif()

# history
add_custom_target(history
  COMMAND ${CMAKE_COMMAND} -E echo "[history] rolling back to last buildable"
  COMMAND ${CMAKE_COMMAND} -E remove_directory "${CMAKE_BINARY_DIR}/gitwc"
  COMMAND git clone --no-checkout "${CMAKE_SOURCE_DIR}" "${CMAKE_BINARY_DIR}/gitwc"
  COMMAND git -C "${CMAKE_BINARY_DIR}/gitwc" bisect start
  COMMAND git -C "${CMAKE_BINARY_DIR}/gitwc" bisect bad HEAD
  COMMAND git -C "${CMAKE_BINARY_DIR}/gitwc" bisect good v0.5
  COMMAND git -C "${CMAKE_BINARY_DIR}/gitwc" bisect run -- cmake --build "${CMAKE_BINARY_DIR}" --target compile
  COMMAND git -C "${CMAKE_BINARY_DIR}/gitwc" bisect reset
  COMMENT "[history] rollback via git bisect"
)


# env
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