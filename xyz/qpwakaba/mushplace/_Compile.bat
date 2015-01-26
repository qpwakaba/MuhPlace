@echo off
cd /d %~dp0
call _Clean.bat
cls
javac -cp ../../../../bukkit.jar;../../../ *.java

if %ERRORLEVEL% neq 0 echo Ž¸”s & pause
exit /b