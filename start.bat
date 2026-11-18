@echo off
cd /d "%~dp0"
if not exist out\classes mkdir out\classes
del /s /q out\classes\*.class >nul 2>&1
(for /r src %%f in (*.java) do @echo "%%f") > out\sources.txt
javac --release 11 -encoding UTF-8 -d out\classes @out\sources.txt
if errorlevel 1 exit /b 1
java -cp out\classes Main %*
