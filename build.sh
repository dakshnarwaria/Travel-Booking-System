#!/usr/bin/env bash
set -e

echo "==================================================="
echo "Compiling Travel Booking System..."
echo "==================================================="

mkdir -p out

javac -encoding UTF-8 -d out -sourcepath src src/com/travel/Main.java src/com/travel/enums/*.java src/com/travel/interfaces/*.java src/com/travel/exception/*.java src/com/travel/util/*.java src/com/travel/seat/*.java src/com/travel/layout/*.java src/com/travel/ui/*.java

echo "Compilation successful!"
echo ""
echo "==================================================="
echo "Running com.travel.Main..."
echo "==================================================="
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -cp out com.travel.Main
