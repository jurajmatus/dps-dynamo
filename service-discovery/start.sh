#!/bin/bash

echo "$(ip a)"
interface=ethwe@

echo "/bin/consul agent -server -bootstrap-expect 1 \
 -node="consul-server"\
 -config-dir="/etc/consul.d"\
 -ui-dir="/ui"\
 -data-dir="/data"\
 -bind="$(ip a | grep -E -A5 "$interface" | grep -Eo "inet ([0-9]+\.){3}[0-9]+" | cut -d " " -f 2 | head -n1)"\
 -advertise="$(ip a | grep -E -A5 "$interface" | grep -Eo "inet ([0-9]+\.){3}[0-9]+" | cut -d " " -f 2 | head -n1)"\
 -client="$(ip a | grep -E -A5 "$interface" | grep -Eo "inet ([0-9]+\.){3}[0-9]+" | cut -d " " -f 2 | head -n1)""

rm -rf /data/*

/bin/consul agent -server -bootstrap-expect 1 \
 -node="consul-server"\
 -config-dir="/etc/consul.d"\
 -ui-dir="/ui"\
 -data-dir="/data"\
 -bind="$(ip a | grep -E -A5 "$interface" | grep -Eo "inet ([0-9]+\.){3}[0-9]+" | cut -d " " -f 2 | head -n1)"\
 -advertise="$(ip a | grep -E -A5 "$interface" | grep -Eo "inet ([0-9]+\.){3}[0-9]+" | cut -d " " -f 2 | head -n1)"\
 -client="$(ip a | grep -E -A5 "$interface" | grep -Eo "inet ([0-9]+\.){3}[0-9]+" | cut -d " " -f 2 | head -n1)"
 
