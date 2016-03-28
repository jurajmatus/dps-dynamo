FROM debian:jessie
    
# Java
RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && \
    echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee /etc/apt/sources.list.d/webupd8team-java.list && \
    echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee -a /etc/apt/sources.list.d/webupd8team-java.list && \
    apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys EEA14886 && \
    apt-get update && \
    apt-get install -y oracle-java8-installer oracle-java8-set-default

# Installations
RUN apt-get update && apt-get install -y rsyslog python python3.4 curl vim

# Rsyslog
ADD ./rsyslog/init-rsyslog.sh /usr/bin/init-rsyslog.sh
ADD ./rsyslog/rsyslog.conf /etc/rsyslog.conf
    
# Dropwizard
ADD ./dropwizard/app.jar /usr/bin/app.jar
ADD ./dropwizard/conf.yaml /etc/conf.yaml
ADD ./dropwizard/run.sh /usr/bin/app.sh

# Consul agent
ADD ./consul/consul /usr/bin/
RUN mkdir -p /tmp/consul
RUN mkdir -p /etc/consul.d
ADD ./consul/config.json /etc/consul.d/

# PORTS

# Dropwizard
EXPOSE 8080

# Consul ports
EXPOSE 8300 8301 8301/udp 8302 8302/udp 8400 8500 8600 8600/udp

# Environment variables for IP setting
ENV NODE1_IP=10.0.0.1
ENV NODE2_IP=10.0.0.6

COPY ./pip/get-pip.py /tmp/get-pip.py
RUN python /tmp/get-pip.py
RUN mkdir -p /var/log/supervisor
RUN pip install supervisor 
COPY ./supervisord.conf /etc/supervisord.conf

ENTRYPOINT ["/usr/local/bin/supervisord"]
