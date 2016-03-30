#!/bin/bash
RSYSLOG_IP="$RSYSLOG_IP"
if [ -z "$RSYSLOG_IP" ] ; then
   RSYSLOG_IP="logging"
fi

killall rsyslogd
sed -i '58s/.*/*.* @'$RSYSLOG_IP'/' /etc/rsyslog.conf
/usr/sbin/rsyslogd -n
