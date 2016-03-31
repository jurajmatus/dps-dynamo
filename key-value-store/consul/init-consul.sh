#!/bin/bash

IP="$(ip a | grep "inet.*eth0" | grep -Eo "inet ([0-9]+\.){3}[0-9]+" | cut -d " " -f 2)"
sed -i 's/127\.0\.0\.1/'$IP'/g' /etc/consul.d/config.json

NODES=""
if [ -n "$NODE1_IP" ] ; then
   NODES="$NODES, \"$NODE1_IP\""
fi
if [ -n "$NODE2_IP" ] ; then
   NODES="$NODES, \"$NODE2_IP\""
fi
if [ -n "$NODES" ] ; then
   NODES=$(echo "$NODES" | sed 's/^,//')
   sed -i 's/\[\]/['"$NODES"']/' /etc/consul.d/config.json
fi

/usr/bin/consul agent -config-dir=/etc/consul.d
