@echo off

pushd "%~dp0"
set JAVA_HOME=
set PATH=%JAVA_HOME%/bin;%PATH%
set CPATH=.;../config/cx_console.properties;


java -Xmx1024m -cp %CPATH% -jar cx_console.jar %*

popd