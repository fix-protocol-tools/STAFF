@echo off
rem SET CLASSPATH=./lib/TaiMI_Parser.jar

if "%1"=="" (
	echo Usage: run.bat script_file.xml
	echo Exit.
	exit
)

..\bin\staff -f %1

