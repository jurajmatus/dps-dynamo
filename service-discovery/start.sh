#!/bin/bash

echo "/bin/consul agent -server -bootstrap \
 -node="consul-server"\
 -config-dir="/etc/consul.d"\
 -ui-dir="/ui"\
 -data-dir="/data"\
 -bind="$(ip a | grep "ethwe" | grep -Eo "inet ([0-9]+\.){3}[0-9]+" | cut -d " " -f 2)"\
 -advertise="$(ip a | grep "ethwe" | grep -Eo "inet ([0-9]+\.){3}[0-9]+" | cut -d " " -f 2)"\
 -client="$(ip a | grep "ethwe" | grep -Eo "inet ([0-9]+\.){3}[0-9]+" | cut -d " " -f 2)""


/bin/consul agent -server -bootstrap \
 -node="consul-server"\
 -config-dir="/etc/consul.d"\
 -ui-dir="/ui"\
 -data-dir="/data"\
 -bind="$(ip a | grep "ethwe" | grep -Eo "inet ([0-9]+\.){3}[0-9]+" | cut -d " " -f 2)"\
 -advertise="$(ip a | grep "ethwe" | grep -Eo "inet ([0-9]+\.){3}[0-9]+" | cut -d " " -f 2)"\
 -client="$(ip a | grep "ethwe" | grep -Eo "inet ([0-9]+\.){3}[0-9]+" | cut -d " " -f 2)"
 
