#!/bin/bash

if [ -n "$LOGGING_IP" ] ; then
   if [ "$(getent hosts logging) | wc -l" -eq 0 ] ; then
      echo "$LOGGING_IP logging" >> /etc/hosts
   fi
fi

/usr/bin/java -jar /usr/bin/app.jar server /etc/conf.yaml
