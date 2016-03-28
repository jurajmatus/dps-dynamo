#!/bin/bash -x

source ../env_var.rc

curl $NODE1_IP:8500/v1/catalog/nodes
curl $NODE2_IP:8500/v1/catalog/nodes
consul members -rpc-addr=$NODE1_IP:8400
consul members -rpc-addr=$NODE2_IP:8400

