#!/bin/bash
RSYSLOG_HOST="$RSYSLOG_HOST"
if [ -z "$RSYSLOG_HOST" ] ; then
   RSYSLOG_HOST="localhost"
fi

echo "*.* @$RSYSLOG_HOST:514" >> /etc/rsyslog.conf

/usr/sbin/rsyslogd -n
