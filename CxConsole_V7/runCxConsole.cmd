@echo off

pushd "%~dp0"
set JAVA_HOME=
set PATH=%JAVA_HOME%/bin;%PATH%
set CPATH=.;../config/cx_console.properties;

set firstparam=%1 %2 %3 %4 %5 %6 %7 %8
set lastparam=%9
if "%~1"=="" goto inteactive
:next
shift /8
if "%~9"=="" goto batch
set lastparam=%lastparam% %9
goto next

:batch
java -Xmx1024m -cp %CPATH% -jar cx_console.jar %firstparam:~% %lastparam:~%
goto cxend

:inteactive
java -Dhttp.auth.preference="NTLM" -cp %CPATH% -jar cx_console.jar

:cxend
popd