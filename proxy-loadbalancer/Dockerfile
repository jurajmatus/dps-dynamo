FROM ubuntu

RUN apt-get update -y
#RUN apt-get install nano -y
RUN apt-get install haproxy -y

# HAPROXY CONFIG
ADD ./haproxy/etc/default/haproxy /etc/default/
ADD ./haproxy/etc/haproxy/haproxy.cfg /etc/haproxy/

# ENV IP
ENV NODE1_IP=10.0.0.8
ENV NODE2_IP=10.0.0.6

# Supervisor
RUN apt-get install -y python python3.4 curl vim
COPY ./pip/get-pip.py /tmp/get-pip.py
RUN python /tmp/get-pip.py
RUN mkdir -p /var/log/supervisor
RUN pip install supervisor
COPY ./supervisord.conf /etc/supervisord.conf

# Consul template
COPY ./consul-template-bin/consul-template /usr/bin/
COPY ./template-files/haproxy.ctmpl /etc/consul-templates/

EXPOSE 8080 443

ENTRYPOINT ["/usr/local/bin/supervisord"]

