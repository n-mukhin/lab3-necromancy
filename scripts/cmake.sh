#!/usr/bin/env bash
set -e

cd "$(dirname "$0")/.."
mkdir -p build
cd build

cmake -DCMAKE_SUPPRESS_REGENERATION=ON ..

cmake --build . --target \
    do_clean \
    compile \
    build \
    test \
    report \
    diff \
    history \
    env
cd ..

