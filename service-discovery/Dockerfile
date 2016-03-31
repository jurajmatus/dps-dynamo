FROM voxxit/consul

RUN mkdir -p /var/log/consul
RUN mkdir -p /etc/consul.d
VOLUME /var/log/consul /etc/consul.d/
ADD ./start.sh /usr/bin/start-consul.sh

ENTRYPOINT ["/usr/bin/start-consul.sh"]

