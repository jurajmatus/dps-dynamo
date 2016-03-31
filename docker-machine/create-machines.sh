#!/bin/bash
docker-machine create\
 -d virtualbox\
 master
 
docker-machine create\
 -d virtualbox\
 --engine-opt="cluster-store=consul://$(docker-machine ip master):8500"\
 --engine-opt="cluster-advertise=eth1:2375"\
 --engine-opt="bip=192.168.101.1/24"\
 --engine-opt="fixed-cidr=192.168.101.0/24"\
 slave
