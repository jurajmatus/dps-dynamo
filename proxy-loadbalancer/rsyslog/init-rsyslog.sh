#!/bin/bash

if [ -n "$LOGGING_IP" ] ; then
   if [ -z "$(getent hosts logging)" ] ; then
      echo "$LOGGING_IP logging" >> /etc/hosts
   fi
fi

killall rsyslogd
/usr/sbin/rsyslogd -n
