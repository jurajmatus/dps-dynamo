#!/bin/bash
docker ps -a | tail -n +2 | awk '{print $NF}' | egrep '(grafana-xxl-storage|grafana)' | xargs -d '\n' -n 1 docker rm -f
