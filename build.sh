#!/bin/bash
GROUP="dps-team11"
VERSION=0.1
cd "$(dirname $0)"
ALL=$(echo *)
for DIR in $ALL ; do
   DF="$DIR/Dockerfile"
   if [ -d "$DIR" ] && [ -f "$DF" ] ; then
      docker build -f "$DF" -t "$GROUP/$DIR:$VERSION" -t "$GROUP/$DIR:latest"
   fi
done
