FROM ubuntu

RUN apt-get update -y
RUN apt-get install -y haproxy rsyslog python python3.4 curl unzip vim python-pip

# Supervisor
RUN mkdir -p /var/log/supervisor
RUN pip install supervisor
COPY ./supervisord.conf /etc/supervisord.conf

# Rsyslog
ADD ./rsyslog/init-rsyslog.sh /usr/bin/init-rsyslog.sh
ADD ./rsyslog/rsyslog.conf /etc/rsyslog.conf

# Consul template
RUN curl -o /tmp/consul-template.zip https://releases.hashicorp.com/consul-template/0.12.1/consul-template_0.12.1_linux_amd64.zip && unzip /tmp/consul-template.zip -d /usr/bin/
COPY ./template-files/haproxy.ctmpl /etc/consul-templates/

# HAPROXY CONFIG
ADD ./haproxy/etc/default/haproxy /etc/default/
ADD ./haproxy/etc/haproxy/haproxy.cfg /etc/haproxy/

EXPOSE 8080 443

ENTRYPOINT ["/usr/local/bin/supervisord"]

