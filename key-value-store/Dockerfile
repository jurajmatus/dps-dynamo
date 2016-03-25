FROM debian:jessie
    
# Java
RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && \
    echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee /etc/apt/sources.list.d/webupd8team-java.list && \
    echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee -a /etc/apt/sources.list.d/webupd8team-java.list && \
    apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys EEA14886 && \
    apt-get update && \
    apt-get install -y oracle-java8-installer oracle-java8-set-default

# Installations
RUN apt-get update && apt-get install -y rsyslog supervisor curl

# Rsyslog
ADD ./rsyslog/init-rsyslog.sh /usr/bin/init-rsyslog.sh
ADD ./rsyslog/rsyslog.conf /etc/rsyslog.conf

# Supervisor
RUN mkdir -p /var/log/supervisor
COPY ./supervisord.conf /etc/supervisor/conf.d/supervisord.conf
    
# Dropwizard
ADD ./dropwizard/app.jar /usr/bin/app.jar
ADD ./dropwizard/conf.yaml /etc/conf.yaml
ADD ./dropwizard/run.sh /usr/bin/app.sh

# PORTS

# Dropwizard
EXPOSE 8080
    
ENTRYPOINT ["/usr/bin/supervisord"]
