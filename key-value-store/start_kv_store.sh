#!/bin/bash -x

compose_file="docker-compose-bridge.yml"

source ../env_var.rc
sed -i "s/NODE1_IP=.*/NODE1_IP=${NODE1_IP}/g" ./Dockerfile
sed -i "s/NODE2_IP=.*/NODE2_IP=${NODE2_IP}/g" ./Dockerfile
sed -i "s/RSYSLOG_IP=.*/RSYSLOG_IP=${RSYSLOG_IP}/g" ./Dockerfile
sed -i "s/LOGGING_IP=.*/LOGGING_IP=${LOGGING_IP}/g" ./Dockerfile

sed -i "s/RSYSLOG_IP: .*/RSYSLOG_IP: '${LOGGING_IP}'/g" ./docker-compose-bridge.yml
sed -i "s/COLLECTD_IP: .*/COLLECTD_IP: '${LOGGING_IP}'/g" ./docker-compose-bridge.yml
sed -i "s/GRAPHITE_IP: .*/GRAPHITE_IP: '${LOGGING_IP}'/g" ./docker-compose-bridge.yml
sed -i "s/LOGGING_IP: .*/LOGGING_IP: '${LOGGING_IP}'/g" ./docker-compose-bridge.yml

sed -i "31s/host: .*/host: ${LOGGING_IP}/" dropwizard/conf.yaml

docker-compose -f $compose_file build
docker-compose -f $compose_file up
#docker-compose --verbose -f $compose_file scale key-value-store=2

