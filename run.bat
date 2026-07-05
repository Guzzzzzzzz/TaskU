@echo off
echo Menyiapkan resource...
if not exist "bin\fonts" xcopy /E /I /Q "src\fonts" "bin\fonts"
if not exist "bin\resources" xcopy /E /I /Q "src\resources" "bin\resources"
echo Menjalankan TaskU...
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot
"%JAVA_HOME%\bin\java" -cp "bin;lib\sqlite-jdbc-3.45.3.0.jar;lib\slf4j-api-2.0.12.jar;lib\slf4j-nop-2.0.12.jar;lib\jbcrypt-0.4.jar" Main
pause
