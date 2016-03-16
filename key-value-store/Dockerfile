FROM debian:jessie

# Supervisor
RUN apt-get update && apt-get install -y supervisor
RUN mkdir -p /var/log/supervisor
COPY ./supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# Collectd
RUN echo "APT::Install-Recommends false;" >> /etc/apt/apt.conf.d/recommends.conf && \
    echo "APT::AutoRemove::RecommendsImportant false;" >> /etc/apt/apt.conf.d/recommends.conf && \
    echo "APT::AutoRemove::SuggestsImportant false;" >> /etc/apt/apt.conf.d/recommends.conf && \
    apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y collectd python-pip ca-certificates && \
    pip install envtpl && \
    apt-get install -y curl && \
    curl -sL https://github.com/tianon/gosu/releases/download/1.4/gosu-amd64 > /usr/bin/gosu && \
    chmod +x /usr/bin/gosu && \
    apt-get remove -y curl && \
    apt-get autoremove -y

COPY ./collector/docker/collectd.conf.tpl /etc/collectd/collectd.conf.tpl
COPY ./collector/docker/run.sh /usr/bin/run-collectd.sh

COPY ./collector /go/src/github.com/bobrik/collectd-docker/collector

RUN apt-get install -y golang-go git && \
    GOPATH=/go go get github.com/bobrik/collectd-docker/collector/... && \
    apt-get remove -y golang-go git && \
    apt-get autoremove -y && \
    mv /go/bin/collector /collector && \
    rm -rf /go && \
    chmod 6755 /collector
    
    
# Java
RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
RUN echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee /etc/apt/sources.list.d/webupd8team-java.list && \
    echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee -a /etc/apt/sources.list.d/webupd8team-java.list && \
    apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys EEA14886 && \
    apt-get update && \
    apt-get install -y oracle-java8-installer oracle-java8-set-default
    
    

    
ENTRYPOINT ["/usr/bin/supervisord"]
