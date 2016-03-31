#!/bin/bash
docker-machine create\
 -d virtualbox\
 master
 
docker-machine create\
 -d virtualbox\
 --engine-opt="cluster-store=consul://$(docker-machine ip master):8500"\
 --engine-opt="cluster-advertise=eth1:2375"\
 slave
