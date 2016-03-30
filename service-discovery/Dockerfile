FROM voxxit/consul

RUN mkdir -p /var/log/consul
RUN mkdir -p /etc/consul.d
VOLUME /var/log/consul /etc/consul.d/
ADD ./start.sh /usr/bin/start-consul.sh

#Already exposed in image
#EXPOSE 8500 8400 8300 8301 8302 8600

#Entrypoint is defined in image
#ENTRYPOINT [ "/bin/consul" ]

#CMD ["agent", "-bootstrap", "-server", "-config-dir=/etc/consul.d", "-ui-dir=/ui"]

