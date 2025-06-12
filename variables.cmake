# variables.cmake
set(SRC_DIR            "${CMAKE_SOURCE_DIR}/src/main/java")
set(TEST_SRC_DIR       "${CMAKE_SOURCE_DIR}/src/test/java")
set(BUILD_DIR          "${CMAKE_SOURCE_DIR}/build")
set(CLASSES_DIR        "${BUILD_DIR}/classes")
set(TEST_CLASSES_DIR   "${BUILD_DIR}/test-classes")
set(DIST_DIR           "${CMAKE_SOURCE_DIR}/dist")
set(LIB_DIR            "${CMAKE_SOURCE_DIR}/lib")
set(SVN_REPO_DIR "${CMAKE_BINARY_DIR}/svnrepo")
set(SVN_WC_DIR   "${CMAKE_BINARY_DIR}/svnwc")

set(CLASSES_OUTPUT_DIR "${CMAKE_BINARY_DIR}/classes")
file(TO_CMAKE_PATH "${SVN_REPO_DIR}" _repo_path)
if(WIN32)
  string(REGEX REPLACE "^([A-Za-z]):/" "/\\1:/" _repo_path "${_repo_path}")
endif()
set(SVN_REPO_URL "file://${_repo_path}")

find_program(SVNADMIN_EXECUTABLE svnadmin REQUIRED)

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

set(WEBAPP_DIR "${CMAKE_SOURCE_DIR}/src/main/webapp")
set(SCP_REMOTE_WAR_DIR "War")


set(SCP_PORT 2222)
set(SCP_USER "s409203")
set(SCP_HOST "se.ifmo.ru")
set(SCP_REMOTE_DIR "Jar")

