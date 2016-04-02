# Dynamo

## Contributors

1. Juraj Matuš
2. Ondrej Vaško
3. Martin Dulovič
4. Kristián Košťál

## Infrastructure

### Service discovery

### Proxy and load balancing

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

TODO: To store the data belonging to the node we use
[Berkeley DB](http://www.oracle.com/technetwork/database/database-technologies/berkeleydb/overview/index.html).

TODO: For asynchronous messaging we use [ActiveMQ](https://github.com/mbknor/dropwizard-activemq-bundle).

### Implementation

#### System interface

TODO: get, put

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
