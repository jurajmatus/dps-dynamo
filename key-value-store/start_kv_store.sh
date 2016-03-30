#!/bin/bash -x

pushd dropwizard
sh build
popd

compose_file="docker-compose-bridge.yml"

docker-compose -f $compose_file build
docker-compose -f $compose_file up
#docker-compose --verbose -f $compose_file scale key-value-store=2

