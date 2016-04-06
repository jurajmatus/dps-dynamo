#!/bin/bash

PROJECT_DIR=$(dirname "$0")
pushd $PROJECT_DIR
source ./common.sh

MACHINES_DIR=~/.docker/machine/machines

export MASTER_MACHINE_IP=$(docker-machine ip master)
export SLAVE_MACHINE_IP=$(docker-machine ip master)

if [ -z $(docker-machine inspect master | grep 'Host does not exist') ] ; then
   docker-machine stop master
   perl -lne '$line=$_; $line =~ s{\$(\w+)}{ exists $ENV{$1} ? $ENV{$1} : q/$/.$1 }ge; $line =~ s{\$\{(\w+)\}}{ exists $ENV{$1} ? $ENV{$1} : q/${/.$1.q/}/ }ge; print "$line";' master.json > "$MACHINES_DIR/master/config.json"
   docker-machine start master
else
   echo "Master doesn't exist. First you have to create machines."
fi

if [ -z $(docker-machine inspect slave | grep 'Host does not exist') ] ; then
   docker-machine stop slave
   perl -lne '$line=$_; $line =~ s{\$(\w+)}{ exists $ENV{$1} ? $ENV{$1} : q/$/.$1 }ge; $line =~ s{\$\{(\w+)\}}{ exists $ENV{$1} ? $ENV{$1} : q/${/.$1.q/}/ }ge; print "$line";' slave.json > "$MACHINES_DIR/slave/config.json"
   docker-machine start slave
else
   echo "Master doesn't exist. First you have to create machines."
fi
