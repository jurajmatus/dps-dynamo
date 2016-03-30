FROM jlachowski/grafana-graphite-statsd

# INSTALLS: Rsyslog, PHP, Apache

RUN apt-get update && \
    add-apt-repository ppa:adiscon/v8-stable && \
    apt-get -y -q install rsyslog && \
    apt-get -y -q install apache2 libapache2-mod-php5 php5-mysql


# CONFIGURATIONS

# Rsyslog
ADD ./rsyslog/rsyslog.conf /etc/rsyslog.conf
ADD ./rsyslog/hostslogs.conf /etc/rsyslog.d/
VOLUME ["/var/log/hosts"]

# Statsd
ADD ./statsd/config.js /src/statsd/config.js

# Whisper, Carbon and Graphite-Web
ADD ./graphite/initial_data.json /opt/graphite/webapp/graphite/initial_data.json
ADD ./graphite/local_settings.py /opt/graphite/webapp/graphite/local_settings.py
ADD ./graphite/carbon.conf /opt/graphite/conf/carbon.conf
ADD ./graphite/storage-schemas.conf /opt/graphite/conf/storage-schemas.conf
ADD ./graphite/storage-aggregation.conf /opt/graphite/conf/storage-aggregation.conf
RUN mkdir -p /opt/graphite/storage/whisper &&\
touch /opt/graphite/storage/graphite.db /opt/graphite/storage/index &&\
chown -R www-data /opt/graphite/storage &&\
chmod 0775 /opt/graphite/storage /opt/graphite/storage/whisper &&\
chmod 0664 /opt/graphite/storage/graphite.db &&\
cd /opt/graphite/webapp/graphite &&\
python manage.py syncdb --noinput

# Grafana
ADD ./grafana/custom.ini /opt/grafana/conf/custom.ini
ADD ./grafana/grafana.db /opt/grafana/data/grafana.db

# Apache
ADD ./apache/start.sh /usr/bin/start-apache.sh
RUN usermod -a -G adm www-data
ADD ./apache/ports.conf /etc/apache2/ports.conf
ADD ./apache/000-default.conf /etc/apache2/sites-available/000-default.conf

# Loganalyzer
RUN rm -rf /var/www & mkdir -p /var/www
ADD ./loganalyzer-3.6.6/src /var/www/
RUN chmod 666 /var/www/config.php

# Nginx and supervisord
ADD ./nginx/nginx.conf /etc/nginx/nginx.conf
ADD ./supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# Consul agent
ADD ./consul/consul /usr/bin/
RUN mkdir -p /tmp/consul
RUN mkdir -p /etc/consul.d
ADD ./consul/config.json /etc/consul.d/
ADD ./consul-services/logging.json /etc/consul.d/

# Environment variables for IP setting
ENV NODE1_IP=192.168.122.84
ENV NODE2_IP=10.0.0.6
ENV RSYSLOG_IP=192.168.122.84

# PORTS

# Rsyslog
EXPOSE 514/tcp
EXPOSE 514/udp

# Http frontends (Grafana, Loganalyzer)
EXPOSE 80

# Graphite listener
EXPOSE 2003



# COMMAND TO RUN

CMD ["/usr/bin/supervisord"]
