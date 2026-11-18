#!/bin/sh
set -eu
cd "$(dirname "$0")"
mkdir -p out/classes
find src -name '*.java' -print > out/sources.txt
find out/classes -type f -name '*.class' -delete
javac --release 11 -encoding UTF-8 -d out/classes @out/sources.txt
java -cp out/classes Main "$@"
