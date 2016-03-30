FROM ubuntu

RUN apt-get update -y
#RUN apt-get install nano -y
RUN apt-get install -y haproxy rsyslog

# Supervisor
RUN apt-get install -y python python3.4 curl vim
COPY ./pip/get-pip.py /tmp/get-pip.py
RUN python /tmp/get-pip.py
RUN mkdir -p /var/log/supervisor
RUN pip install supervisor
COPY ./supervisord.conf /etc/supervisord.conf

# Rsyslog
ADD ./rsyslog/init-rsyslog.sh /usr/bin/init-rsyslog.sh
ADD ./rsyslog/rsyslog.conf /etc/rsyslog.conf

# Consul template
COPY ./consul-template-bin/consul-template /usr/bin/
COPY ./template-files/haproxy.ctmpl /etc/consul-templates/

# HAPROXY CONFIG
ADD ./haproxy/etc/default/haproxy /etc/default/
ADD ./haproxy/etc/haproxy/haproxy.cfg /etc/haproxy/

# ENV IP
ENV NODE1_IP=192.168.122.84
ENV NODE2_IP=10.0.0.6
ENV RSYSLOG_IP=192.168.122.84

EXPOSE 8080 443

ENTRYPOINT ["/usr/local/bin/supervisord"]

