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

source ../env_var.rc
docker-compose build

#sleep 8
#docker network create -d overlay --subnet=10.0.100.0/24 dynamo-net

