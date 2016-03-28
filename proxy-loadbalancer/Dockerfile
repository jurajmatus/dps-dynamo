FROM ubuntu

RUN apt-get update -y
RUN apt-get install nano -y
RUN apt-get install haproxy -y


ADD ./etc/default/haproxy /etc/default/haproxy
ADD ./etc/haproxy/haproxy.cfg /etc/haproxy/haproxy.cfg

VOLUME /var/log/consul /etc/consul.d/

EXPOSE 80 443