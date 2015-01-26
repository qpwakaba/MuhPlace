@echo off
SET outputFile=MushPlace.jar
SET nowDir=%CD%
call xyz\qpwakaba\mushplace\_Compile.bat 
if %ERRORLEVEL% neq 0 cd /d %nowDir% & goto ENDOFBATCH
cd /d %nowDir%
cls
echo classÉtÉ@ÉCÉãâªÇ…ÇÕê¨å˜
jar -cvf %outputFile% .\xyz\ plugin.yml history.txt || pause

:ENDOFBATCH