#!/bin/bash

source ../env_var.rc
sed -i "s/NODE1_IP=.*/NODE1_IP=${NODE1_IP}/g" ./Dockerfile
sed -i "s/NODE2_IP=.*/NODE2_IP=${NODE2_IP}/g" ./Dockerfile
docker-compose build
docker-compose up
