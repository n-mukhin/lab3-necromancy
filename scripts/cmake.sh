#!/bin/bash
cd "$(dirname "$0")/.."
mkdir -p build
cd build

cmake -DCMAKE_SUPPRESS_REGENERATION=ON ..

cmake --build . --target do_clean
cmake --build . --target compile || # cmake --build . --target history
cmake --build . --target build
# cmake --build . --target music
cmake --build . --target test
cmake --build . --target report
# cmake --build . --target scp
# cmake --build . --target diff
cmake --build . --target env
