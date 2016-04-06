#!/bin/bash

function readip {
   ip a | grep "inet.*$1" | grep -Eo "inet ([0-9]+\.){3}[0-9]+" | cut -d " " -f 2
}

export HOST_IP=$(readip "eth0")
export INTERFACE="eth0"
if [ -z "$HOST_IP" ] ; then
   export HOST_IP=$(readip "wlan0")
   export INTERFACE="wlan0"
fi
