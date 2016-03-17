#!/bin/bash
GRAPHITE_HOST="$GRAPHITE_HOST"
if [ -n "$GRAPHITE_HOST" ] ; then
   GRAPHITE_HOST="http://localhost"
fi
sed -i -e 's/127\.0\.0\.1/'$GRAPHITE_HOST'/g' /src/statsd/config.js

/usr/bin/node /src/statsd/stats.js /src/statsd/config.js
