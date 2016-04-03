#!/bin/bash

/bin/consul agent -server -bootstrap \
 -node="${HOSTNAME}-consul-server"\
 -config-dir="/etc/consul.d"\
 -ui-dir="/ui"\
 -data-dir="/data"\
 -advertise="${HOST_IP}"\
 -client="${HOST_IP}"\
 -join="${NODE1_IP}"\
#  -join="${NODE2_IP}"
