#!/bin/bash

eval $(docker-machine env master)
docker-compose -f master.yml up

eval $(docker-machine env master)
docker-compose -f slave.yml scale key-value-store=2
