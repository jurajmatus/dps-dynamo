#!/bin/bash

PROJECT_DIR=$(dirname "$0")
pushd $PROJECT_DIR
source ./common.sh

MACHINES_DIR=~/.docker/machine/machines

if [ -z $(docker-machine inspect master | grep 'Host does not exist') ] ; then
   docker-machine stop master
   cp master.json "$MACHINES_DIR/master/config.json"
   docker-machine start master
else
   echo "Master doesn't exist. First you have to create machines."
fi

if [ -z $(docker-machine inspect slave | grep 'Host does not exist') ] ; then
   docker-machine stop slave
   cp slave.json "$MACHINES_DIR/slave/config.json"
   docker-machine start slave
else
   echo "Master doesn't exist. First you have to create machines."
fi
