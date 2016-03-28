#!/bin/bash -x

###
#Docker daemon must be run with options:
#	-H tcp://0.0.0.0:2375 
#	-H unix:///var/run/docker.sock 
#	--cluster-store=consul://$CONSUL_IP:8500 
#	--cluster-advertise=$INTERFACE:2375
#	--bip=<IP_A>.1/24 
#	--fixed-cidr=<IP_A>.0/24
##
#eg. docker daemon --log-driver=journald -H tcp://0.0.0.0:2375 -H unix:///var/run/docker.sock --bip=192.168.101.1/24 --fixed-cidr=192.168.101.0/24 --cluster-store=consul://10.0.0.1:8500 --cluster-advertise=eth0:2375 &> /tmp/docker.log &
###

source ../env_var.rc

##
#build and run image with compose and redirect output to /tmp/consul_server.log
##
docker-compose up &> /tmp/consul_server.log &

