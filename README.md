# Dynamo

## Contributors

1. Juraj Matuš
2. Ondrej Vaško
3. Martin Dulovič
4. Kristián Košťál

## Usage

### Prerequisites

To run this application, you need to have basic docker tooling installed:
* [docker](https://docs.docker.com/engine/installation/)
* [docker-compose](https://docs.docker.com/compose/install/)
* [docker-machine](https://docs.docker.com/machine/install-machine/)
* [weave](https://github.com/weaveworks/weave#installation)

### Build & run

Download the repository and create docker machines:

```bash
git clone https://github.com/jurajmatus/dps-dynamo.git
cd dps-dynamo
```

Deploying Master
```bash
docker-machine create -d virtualbox master
docker-machine ssh master
sudo -i
echo 1 > /proc/sys/net/ipv4/conf/all/proxy_arp  # routes ARP requests from docker-machine to docker containers
eval $(docker-machine env master)
weave launch
eval "$(weave env)"
docker-compose -f master.yml build
docker-compose -f master.yml up
#weave expose
```

Cleaning Master
```bash
# In other shell than it was deployed in
docker-compose -f master.yml rm --all
weave stop
weave reset
eval "$(weave env --restore)"
```

Deploying Slave
```bash
docker-machine create -d virtualbox slave
docker-machine ssh slave
sudo -i
echo 1 > /proc/sys/net/ipv4/conf/all/proxy_arp  # routes ARP requests from docker-machine to docker containers
eval $(docker-machine env slave)
weave launch $(docker-machine ip master)
eval "$(weave env)"
docker-compose -f slave.yml build
docker-compose -f slave.yml scale key-value-store=2
#weave expose
```

Cleaning Slave
```bash
docker-compose -f slave.yml scale key-value-store=0
weave stop
weave reset
eval "$(weave env --restore)"
```

Setup Host and test
```bash
ip r add 10.32.0.0/12 dev vboxnet0	# adds route to Weave network from host computer
curl $(weave dns-lookup consul-server):8500/v1/catalog/nodes | python -m json.tool
curl $(weave dns-lookup consul-server):8500/v1/health/service/dynamo | python -m json.tool
curl $(weave dns-lookup haproxy):8080/check_connectivity
#open logging in web browser: firefox http://$(weave dns-lookup logging-server)/login, firefox http://$(weave dns-lookup logging-server)/loganalyzer
```

### API

Firstly you need to know an address of application's end-point. All addresses listed will be relative to this:
```bash
docker-machine ip master
```

#### Get

URL: /storage/{key}?minNumReads={minNumReads}

Method: GET

Parameters:

* _key_ : String - BASE64 encoded byte array
* _minNumReads_ : Integer - minimal number of replicas to acknowledge the request so that response could be sent

Response body:

* _key_ : String - BASE64 encoded byte array
* _value_ : Object
  * _version_ : String - version string
  * _values_ : Array[String] - BASE64 encoded byte arrays, one for each unresolved version

#### Put

URL: /storage/

Method: PUT

Content-type: application/json

Body:

* _key_ : String - BASE64 encoded byte array
* _value_ : String - BASE64 encoded byte array
* _fromVersion_ : String - version string, exactly as received from the last get, or empty
* _minNumWrites_ : Integer - minimal number of replicas to acknowledge the request so that write was considered complete

Response body:

* _success_ : Boolean - success of operation. Will be false if old value of _fromVersion_ is used, or if any other error occurs

#### Delete

There is no method to specifically delete an entry. To do so, Put method where value is empty string has to be issued.

## Infrastructure

### Service discovery

### Proxy and load balancing

[HaProxy](http://www.haproxy.org) with Consul Template

### Logging

For distributed logging we use a central [rsyslog](http://www.rsyslog.com) daemon collecting the log entries from all hosts.

Each node then runs a local rsyslog that listens to application and pushes the logs to the central rsyslog over UDP in batches.
That's achieved by using a queue. To handle temporary connection failures, daemon is configured to attempt retry if the send fails.

To view and filter messages we use web based front-end [loganalyzer](http://loganalyzer.adiscon.com/).

### Metrics and monitoring

We use [Graphite](https://graphite.readthedocs.org/en/latest/) to collect metrics.
Those are then viewed in [Grafana](http://grafana.org/).

Producing and sending metrics in an appropriate format is handled by
[Dropwizard metrics](https://dropwizard.github.io/metrics/3.1.0/).
It automatically generates various JVM performance metrics and allows to configure custom metrics
in many representations, like counters, timers, histograms, etc.

### Orchestration

## Application

The main Dynamo application runs on top of [Dropwizard](http://www.dropwizard.io/0.9.2/docs/) framework.

### Underlying systems

Http requests are handled by server [Jetty](http://www.eclipse.org/jetty/) and REST framework [Jersey](https://jersey.java.net/). All requests are handled asynchronously, using [JAX-RS @Suspended annotation](https://jersey.java.net/documentation/latest/async.html).

To store the data belonging to the node we use
[Berkeley DB](http://www.oracle.com/technetwork/database/database-technologies/berkeleydb/overview/index.html).

For asynchronous messaging we use [ActiveMQ](http://activemq.apache.org/). Every node runs an embedded instance.

### Implementation

#### Partitioning

TODO

#### Versioning

TODO

#### Execution

TODO

#### Handling failure

TODO: temporary, permanent

#### Membership and failure detection

TODO

#### Adding and removing the nodes

TODO
