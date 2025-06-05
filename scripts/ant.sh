#!/bin/bash
cd "$(dirname "$0")/.."
ant clean
ant compile || ant history
ant build
ant test
ant report
# ant scp
ant diff
ant env
