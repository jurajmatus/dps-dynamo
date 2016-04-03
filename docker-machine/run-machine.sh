#!/bin/bash

PROJECT_DIR=$(dirname "$0")
pushd $PROJECT_DIR

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
   eval "$(docker-machine env master)"
   docker-compose -f master.yml up
elif [ "$1" == "slave" ] ; then
   eval "$(docker-machine env slave)"
   docker-compose -f slave.yml scale key-value-store=2
else
   echo "Unsuported machine $1"
fi
