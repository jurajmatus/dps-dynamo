#!/bin/bash 

proxy=$(weave dns-lookup haproxy | head -n1)

for i in $(seq 1 10000); do 

	#key=$(echo "key$(cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 43 | head -n 1)" | base64)
	#value=value$(cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 10 | head -n 1)
  key=$(cat /dev/urandom | tr -dc 'a-z' | fold -w 10 | head -n 1)
	value=$(cat /dev/urandom | tr -dc 'a-z' | fold -w 10 | head -n 1)
	curl -H 'Content-Type: application/json' -X PUT -d '
	{
	  "key": "'"$key"'",
	  "value": "'"$value"'",
	  "fromVersion": "",
	  "minNumWrites": 1
	}' $proxy:8080/storage/
	sleep 0.05

done
