#!/bin/sh
set -eu
cd "$(dirname "$0")"
rm -rf out
mkdir -p out
find src -name '*.java' > fuentes.txt
javac -encoding UTF-8 -d out @fuentes.txt
cp -R assets out/assets
jar cfe MazmorrasUNSA.jar Main -C out .
echo "Compilacion correcta: MazmorrasUNSA.jar"
