#!/bin/bash

PROJECT_DIR=$(dirname "$0")
pushd $PROJECT_DIR/..

if [ -z "$1" ] ; then
   echo "Usage: run-machine.sh master|slave"
   exit
fi

function readip {
   ip a | grep "inet.*$1" | grep -Eo "inet ([0-9]+\.){3}[0-9]+" | cut -d " " -f 2
}

export NODE1_IP="$(docker-machine ip master)"
export LOGGING_IP="$(docker-machine ip master)"
export HOST_IP=$(readip "eth0")
if [ -z "$HOST_IP" ] ; then
   export HOST_IP=$(readip "wlan0")
fi


if [ "$1" == "master" ] ; then
   SERVICES="service-discovery proxy-loadbalancer logging"
   IP="$(docker-machine ip master)"
   eval "$(docker-machine env master)"
elif [ "$1" == "slave" ] ; then
   SERVICES="key-value-store"
   IP="$(docker-machine ip slave)"
   eval "$(docker-machine env master)"
fi

echo "Started in virtual machine with IP: $IP"

for SERVICE in $SERVICES
do
   pushd $SERVICE
   find . -iname '*build*' -executable | xargs -d '\n' -n 1 --no-run-if-empty sh
   docker-compose build
   LOGFILE="/var/log/docker-machine-$SERVICE.log"
   docker-compose up &> $LOGFILE
   echo "Started $SERVICE logging to $LOGFILE"
   popd
done
