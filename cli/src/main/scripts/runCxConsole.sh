#!/bin/bash
pushd "`dirname \"$0\"`"
java -Xmx1024m -cp :./config/cx_console.properties -jar cx_console.jar $@
popd
