# Dynamo

## Contributors

1. Juraj Matuš
2. Ondrej Vaško
3. Martin Dulovič
4. Kristián Košťál

## Usage

### Prerequisites

To run this application, docker, docker-compose and docker-machine are required.
Preinstallation scripts are written in bash.

### Build & run

Download the repository and create docker machines:

```bash
git clone https://github.com/jurajmatus/dps-dynamo.git
cd dps-dynamo
sh docker-machine/create-machines.sh
```

Afterwards, you can run them in separate terminal sesions:
```bash
sh docker-machine/run-machine.sh master
```
```bash
sh docker-machine/run-machine.sh slave
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

#### Put

URL: /storage/

Method: PUT

Content-type: application/json

Body:

* _key_ : String - BASE64 encoded byte array
* _value_ : String - BASE64 encoded byte array
* _fromVersion_ : String - version string, exactly as received from the last get, or empty
* _minNumWrites_ : Integer - minimal number of replicas to acknowledge the request so that write was considered complete

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
