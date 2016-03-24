#!/bin/bash -x

#export NODE1_IP=10.0.0.1
#export NODE2_IP=10.0.0.6
source ../env_var.rc

curl $NODE1_IP:8500/v1/catalog/nodes
curl $NODE2_IP:8500/v1/catalog/nodes
consul members -rpc-addr=$NODE1_IP:8400
consul members -rpc-addr=$NODE2_IP:8400

