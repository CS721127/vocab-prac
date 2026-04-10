@echo off
echo Building VocabMaster for Windows...

:: 1. Compile
javac *.java
if %errorlevel% neq 0 exit /b %errorlevel%

:: 2. Create JAR
jar cfe VocabApp.jar SwingApp *.class
if %errorlevel% neq 0 exit /b %errorlevel%

:: 3. Bundle with jpackage
:: Ensure you have a JDK with jpackage installed (JDK 14+)
:: This will create an .exe installer in the 'dist' folder
jpackage --name VocabMaster --input . --main-jar VocabApp.jar --main-class SwingApp --type app-image --dest dist --win-console

echo Build Complete! Check the 'dist' folder.
pause
