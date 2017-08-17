@echo off

pushd "%~dp0"
set JAVA_HOME=
set PATH=%JAVA_HOME%/bin;%PATH%
set PROPPATH=/config/cx_console.properties;

java -Xmx1024m -jar cx_console.jar -cp %PROPPATH% %*

popd