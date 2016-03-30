#!/bin/bash
RSYSLOG_HOST="$RSYSLOG_HOST"
if [ -z "$RSYSLOG_HOST" ] ; then
   RSYSLOG_HOST="logging"
fi

if [ "$RSYSLOG_IP" ] ; then
   RSYSLOG_HOST="$RSYSLOG_IP"
fi

killall rsyslogd
sed -i '58s/.*/*.* @'$RSYSLOG_HOST'/' /etc/rsyslog.conf
/usr/sbin/rsyslogd -n
