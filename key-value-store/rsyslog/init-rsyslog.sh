#!/bin/bash
RSYSLOG_HOST="$RSYSLOG_HOST"
if [ -n "$RSYSLOG_HOST" ] ; then
   RSYSLOG_HOST="http://localhost"
fi
sed -i -e 's/__HOST__/'$RSYSLOG_HOST'/g' /etc/rsyslog.conf

/usr/sbin/rsyslogd -n
