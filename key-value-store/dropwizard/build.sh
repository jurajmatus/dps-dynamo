#!/bin/bash
mvn package
cp "$(find target -iname 'KeyValueStore*.jar')" "app.jar"
