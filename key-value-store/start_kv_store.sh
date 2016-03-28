#!/bin/bash -x

compose_file="docker-compose-bridge.yml"

source ../env_var.rc
sed -i "s/NODE1_IP=.*/NODE1_IP=${NODE1_IP}/g" ./Dockerfile
sed -i "s/NODE2_IP=.*/NODE2_IP=${NODE2_IP}/g" ./Dockerfile
docker-compose -f $compose_file build
#docker-compose up
docker-compose --verbose -f $compose_file scale key-value-store=2

