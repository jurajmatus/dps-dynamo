#!/bin/bash
pushd "$(dirname $0)"
mvn package
cp "$(find target -iname 'KeyValueStore*.jar')" "app.jar"
popd
