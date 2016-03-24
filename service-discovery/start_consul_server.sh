#!/bin/bash -x

###
#Docker daemon must be run with options:
#	-H tcp://0.0.0.0:2375 
#	-H unix:///var/run/docker.sock 
#	--cluster-store=consul://$CONSUL_IP:8500 
#	--cluster-advertise=$INTERFACE:2375
##
#eg. "docker daemon --log-driver=journald -H tcp://0.0.0.0:2375 -H unix:///var/run/docker.sock --cluster-store=consul://10.0.0.1:8500 --cluster-advertise=eth0:2375 &> docker.log &"
###

##
#export NODE1_IP=10.0.0.1
#export NODE2_IP=10.0.0.6
#export INTERFACE="eth0"
#export HOST_IP=$(ip a | grep "inet.*$INTERFACE" | grep -Eo "inet ([0-9]+\.){3}[0-9]+" | cut -d " " -f 2)
##
source ../env_var.rc

##
#build and run image with compose and redirect output to /tmp/consul_server.log
##
#docker build -t voxxit_consul_server:latest .
docker-compose up &> /tmp/consul_server.log &
sleep 5

#join cluster
consul members

