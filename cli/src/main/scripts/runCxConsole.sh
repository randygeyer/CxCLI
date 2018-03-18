#!/bin/bash
pushd  "`dirname \"$0\"`"
java -Xmx1024m -jar cx_console.jar "$@"
set exitCode=%errorlevel%
popd
Exit /B %exitCode%