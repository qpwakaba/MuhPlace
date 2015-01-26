@echo off
SET outputFile=MushPlace.jar
SET outputFile=release\%outputFile%
jar -cvf %outputFile% .\xyz\qpwakaba\untp\*.class plugin.yml history.txt || pause

:ENDOFBATCH