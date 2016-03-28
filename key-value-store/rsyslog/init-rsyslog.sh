#!/bin/bash
RSYSLOG_HOST="$RSYSLOG_HOST"
if [ -z "$RSYSLOG_HOST" ] ; then
   RSYSLOG_HOST="logging"
fi

killall rsyslogd
sed -i '58s/.*/*.* @'$RSYSLOG_HOST'/' /etc/rsyslog.conf
/usr/sbin/rsyslogd -n
