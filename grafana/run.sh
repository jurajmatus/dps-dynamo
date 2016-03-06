#!/bin/bash

cd "$(dirname $0)"
sh clean.sh

docker run -d -v /tmp/grafana --name grafana-xxl-storage busybox:latest

docker run \
  -d \
  -p 3000:3000 \
  --name grafana-xxl \
  -e "GF_SECURITY_ADMIN_USER=admin" \
  -e "GF_SECURITY_ADMIN_PASSWORD=long-password" \
  --volumes-from grafana-xxl-storage \
  --name grafana \
  monitoringartist/grafana-xxl
