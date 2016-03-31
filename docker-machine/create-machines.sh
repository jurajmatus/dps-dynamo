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

docker-machine create\
 -d virtualbox\
 --engine-opt="H tcp://0.0.0.0:2375"\
 --engine-opt="H unix:///var/run/docker.sock"\
 --engine-opt="cluster-advertise=$INTERFACE:2375"\
 --engine-opt="bip=192.168.101.1/24"\
 --engine-opt="fixed-cidr=192.168.101.0/24"\
 master
 
docker-machine create\
 -d virtualbox\
 --engine-opt="H tcp://0.0.0.0:2375"\
 --engine-opt="H unix:///var/run/docker.sock"\
 --engine-opt="cluster-store=consul://$(docker-machine ip master):8500"\
 --engine-opt="cluster-advertise=$INTERFACE:2375"\
 --engine-opt="bip=192.168.101.1/24"\
 --engine-opt="fixed-cidr=192.168.101.0/24"\
 slave
