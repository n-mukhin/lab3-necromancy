# ---- variables.cmake ----

# Project metadata
set(PROJECT_NAME    "lab3-beta")
set(PROJECT_VERSION "0.4")

# Source & build directories
set(SRC_DIR      "${CMAKE_SOURCE_DIR}/src/main/java")
set(BUILD_DIR    "${CMAKE_BINARY_DIR}/build")
set(CLASSES_DIR  "${BUILD_DIR}/classes")

# Distribution & library
set(DIST_DIR     "${CMAKE_SOURCE_DIR}/dist")
set(LIB_DIR      "${CMAKE_SOURCE_DIR}/lib")

# JAR output
set(JAR_NAME     "${PROJECT_NAME}-${PROJECT_VERSION}.jar")
set(JAR_FILE     "${DIST_DIR}/${JAR_NAME}")

# SCP parameters (not used by CMake directly, but kept for reference)
set(SCP_USER     "s409203")
set(SCP_HOST     "se.ifmo.ru")
set(SCP_PORT     "2222")
set(SCP_REMOTE_DIR "Jar")

# Localization directories (for native2ascii)
set(LOCALES_SRC_DIR   "${CMAKE_SOURCE_DIR}/src/main/resources")
set(LOCALES_DEST_DIR  "${BUILD_DIR}/locales-escaped")

# Checksums, Javadoc, Manifest paths
set(CHECKSUM_DIR      "${BUILD_DIR}/checksums")
set(JAVADOC_OUT       "${BUILD_DIR}/javadoc")
set(MANIFEST_FILE     "${BUILD_DIR}/generated-manifest.mf")
