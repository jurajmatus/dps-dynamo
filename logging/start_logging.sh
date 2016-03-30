#!/bin/bash

source ../env_var.rc
docker-compose build
#docker-compose up &> /tmp/logging.log &
docker-compose up
