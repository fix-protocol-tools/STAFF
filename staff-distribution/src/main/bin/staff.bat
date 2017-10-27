@echo off

if "%JAVA_HOME%" == "" goto setJavaBinWithoutHome
set JAVA_BIN="%JAVA_HOME%\bin\java.exe"
goto endJavaHome

:setJavaBinWithoutHome
set JAVA_BIN="java.exe"

:endJavaHome

set STAFF_HOME_DIR=%~dp0..

set CLASSPATH="%STAFF_HOME_DIR%\lib\ant-launcher-${ant.version}.jar"
set JAVA_OPTS="-Dstaff.log.dir=%STAFF_HOME_DIR%\logs" "-Dant.home=%STAFF_HOME_DIR%" "-Djava.library.path=%STAFF_HOME_DIR%\lib"
%JAVA_BIN% -classpath %CLASSPATH% %JAVA_OPTS% org.apache.tools.ant.launch.Launcher %* -cp "%STAFF_HOME_DIR%\lib\staff-ant-annotations-${project.version}.jar;%STAFF_HOME_DIR%\lib\staff-core-${project.version}.jar;%STAFF_HOME_DIR%\config"
