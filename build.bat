@echo off
setlocal

echo ===================================================
echo Compiling Travel Booking System...
echo ===================================================

if not exist out mkdir out

REM Enable UTF-8 code page for Unicode box characters
chcp 65001 > nul

javac -encoding UTF-8 -d out -sourcepath src src\com\travel\Main.java src\com\travel\enums\*.java src\com\travel\interfaces\*.java src\com\travel\exception\*.java src\com\travel\util\*.java src\com\travel\seat\*.java src\com\travel\layout\*.java src\com\travel\ui\*.java

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    exit /b %ERRORLEVEL%
)

echo Compilation successful!
echo.
echo ===================================================
echo Running com.travel.Main...
echo ===================================================
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -cp out com.travel.Main

endlocal
