#!/bin/bash

PROJECT_DIR=$(dirname "$0")
pushd $PROJECT_DIR
source ./common.sh

if [ -z "$1" ] ; then
   echo "Usage: run-machine.sh master|slave"
   exit
fi

export NODE1_IP="$(docker-machine ip master)"
export LOGGING_IP="$(docker-machine ip master)"

if [ "$1" == "master" ] ; then

   docker-machine start master
   eval "$(docker-machine env master)"
   docker-compose -f master.yml up
   
elif [ "$1" == "slave" ] ; then

   docker-machine start slave
   eval "$(docker-machine env slave)"
   pushd ../key-value/store/dropwizard
   sh build.sh
   docker-compose -f slave.yml scale key-value-store=2
   popd
   
else
   echo "Unsuported machine $1"
fi
