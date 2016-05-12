FROM debian:jessie
    
# Java
RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && \
    echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee /etc/apt/sources.list.d/webupd8team-java.list && \
    echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee -a /etc/apt/sources.list.d/webupd8team-java.list && \
    apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys EEA14886 && \
    apt-get update && \
    apt-get install -y oracle-java8-installer oracle-java8-set-default

# Installations
RUN apt-get update && apt-get install -y rsyslog python python3.4 curl vim python-pip unzip
RUN mkdir -p /var/log/supervisor
RUN pip install supervisor 
    
# Consul agent
#RUN curl -o /tmp/consul.zip https://releases.hashicorp.com/consul/0.6.4/consul_0.6.4_linux_amd64.zip && unzip /tmp/consul.zip -d /usr/bin/
#RUN mkdir -p /tmp/consul
#RUN mkdir -p /etc/consul.d

# Supervisor
COPY ./supervisord.conf /etc/supervisord.conf

# Rsyslog
ADD ./rsyslog/rsyslog.conf /etc/rsyslog.conf
    
# Dropwizard
ADD ./dropwizard/app.jar /usr/bin/app.jar
ADD ./dropwizard/conf.yaml /etc/conf.yaml
ADD ./dropwizard/run.sh /usr/bin/app.sh

#ADD ./consul/config.json /etc/consul.d/
#ADD ./consul-services/dps-dynamo.json /etc/consul.d/

ENTRYPOINT ["/usr/local/bin/supervisord"]
