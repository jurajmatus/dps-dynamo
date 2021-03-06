# Dynamo

## Contributors

1. Juraj Matuš
2. Ondrej Vaško
3. Martin Dulovič
4. Kristián Košťál

## Usage

### Prerequisites

To build and run this application, you need to have basic docker tooling installed:
* [maven](https://maven.apache.org/install.html)
* [docker](https://docs.docker.com/engine/installation/)
* [docker-compose](https://docs.docker.com/compose/install/)
* [docker-machine](https://docs.docker.com/machine/install-machine/)
* [weave](https://www.weave.works/docs/net/latest/installing-weave/)

### Build & run

Download the repository and create docker machines:

```bash
git clone https://github.com/jurajmatus/dps-dynamo.git
cd dps-dynamo/docker-machine-weave
```

The default configuration is for the application to be split into two machines:
* Master
  * service discovery
  * logging and monitoring service
* Slave
  * Dynamo nodes

To build and run the master, run the following commands:
```bash
docker-machine create -d virtualbox master
# routes ARP requests from docker-machine to docker containers
docker-machine ssh master
 sudo -i
  echo 1 > /proc/sys/net/ipv4/conf/all/proxy_arp
  exit
 exit
eval $(docker-machine env master)
weave launch
eval "$(weave env)"
docker stop $(docker ps -a | grep "weaveplugin\|weavevolumes\|weavedb" | cut -f 1 -d " ")
docker-compose -f master.yml build

# Run - still from the same shell
docker-compose -f master.yml up
weave expose
```

To build and run the slave, run the following commands:
```bash
docker network create --driver=bridge dockermachineweave_default
docker-machine create -d virtualbox slave
# routes ARP requests from docker-machine to docker containers
docker-machine ssh slave
 sudo -i
  echo 1 > /proc/sys/net/ipv4/conf/all/proxy_arp
  exit
 exit
eval $(docker-machine env slave)
weave launch $(docker-machine ip master)
eval "$(weave env)"
docker stop $(docker ps -a | grep "weaveplugin\|weavevolumes\|weavedb" | cut -f 1 -d " ")
bash ../key-value-store/dropwizard/build.sh     
docker-compose -f slave.yml build

# Run - still from the same shell
docker-compose -f slave.yml scale key-value-store=2
weave expose
```

Cleaning Master
```bash
# In other shell than it was deployed in
docker-compose -f master.yml rm --all
weave stop
weave reset
eval "$(weave env --restore)"
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
curl $(weave dns-lookup consul-server.weave.local | head -n1):8500/v1/catalog/nodes | python -m json.tool
curl $(weave dns-lookup consul-server.weave.local | head -n1):8500/v1/health/service/dynamo | python -m json.tool
curl $(weave dns-lookup haproxy.weave.local | head -n1):8080/check_connectivity
#open logging in web browser: firefox http://$(weave dns-lookup logging-server.weave.local | head -n1)/login, firefox http://$(weave dns-lookup logging-server.weave.local | head -n1)/loganalyzer
```

### API

Firstly you need to know an address of application's end-point. All addresses listed will be relative to this:
```bash
weave dns-lookup haproxy
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

Values are partitioned based on their key. The key is hashed via **md5** function and mapped into consistent hash space of 64 bits.

#### Versioning

Each value has a version string associated with it, which is internally a **vector clock**.
Vector clock contains up to 10 entries of node's ip address, version number and timestamp. Timestamp is not used in conflict resolution algortithm, it only serves to find oldest entries when trimming is performed.

#### Execution

Upon receiving of a request, the coordinator for the key is computed. If the receiving node is not responsible for it, the request is **redirected** to one of the responsible nodes via http protocol.

If the node is responsible, **request id** is generated for tracking and **state machine** is created for the request.
All subsequent operations are then offloaded to **message queue** workers.

Nodes responsible for the key are computed and contacted via message queue to either replicate (PUT) or provide the value (GET). Version resolution is performed before writes and after collecting all reads. Based on the result of it, either the value is used as is, is merged or is rejected.

Internaly reads and writes are provided by BerkeleyDB. Reads are done non-transactionally. Writes are done **optimistically** - read is done, operations like version resolution are done, and then the value is written if change hasn't been done in the meantime. The last read-write is done in one transaction. If write fails, the whole sequence is repeated. This strategy was chosen to provide fast reads, but at the expense of possibility of failed writes.

Upon receiving the **acknowledgement** number _w_ or _r_, response is sent back to the client. Timeout is checked. If it elapses, the topology is recomputed and new responsible nodes are contacted.

Upon receiving the acknowledgement number _n_, state machine is discarded from internal storage. If timeout is exceeded, http error code is sent back to the client.

#### Membership and failure detection
Membership detection is done via service discovery in our case consul server. All nodes are registered in consul with their hostnames, IP adresses and positions in Dynamo. That means, all nodes can query consul for active and failing dynamo services and their positions. 

Application contacts consul every 3 seconds and asks for active and failing nodes and compares last active topology with result of query. If there is new active node or some of active nodes is failing, the topology is recreated and keys, which need to be redistributed to new nodes are redistributed with keeping replication active and keys from failing node which are replicated are moved from replicators to new responsible node within recreated topology.

#### Handling failure
Handling failure is done via replicator nodes. According to number of replication nodes parameter, there can be multiple replication nodes. In case of finding a node failure, the replicated data are send from first available replicator node in Dynamo (Chord) topology to node which will be responsible for failed node keys. 

Finding keys from failed node is easy, because we keep track of all nodes and their positions. So keys from failed node are keys lower than position of failed node and higher than position of node previous to failed node in sorted set. 
