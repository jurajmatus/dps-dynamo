#!/bin/bash
echo "${LOGGING_IP} logging" >> /etc/hosts
/usr/bin/java -jar /usr/bin/app.jar server /etc/conf.yaml
