FROM jlachowski/grafana-graphite-statsd



# INSTALLS

RUN apt-get update

# Rsyslog
RUN add-apt-repository ppa:adiscon/v8-stable
RUN apt-get -y -q install rsyslog

# Mysql
ENV MYSQL_ROOT_PASSWORD=mysql-password
ENV MYSQL_DATABASE=db
ENV MYSQL_USER=mysql
ENV MYSQL_PASSWORD=mysql-password
ENV MYSQL_DATA_DIR=/var/lib/mysql

RUN apt-get -y -q install mysql-server
RUN rm -rf ${MYSQL_DATA_DIR}

# PHP
RUN apt-get -y -q install php5-cli php5-fpm php5-mysql



# CONFIGURATIONS

# Mysql
ADD ./mysql/entrypoint.sh /usr/bin/mysql-entrypoint.sh
RUN chmod +x /usr/bin/mysql-entrypoint.sh
RUN RUN mkdir /docker-entrypoint-initdb.d
VOLUME ["${MYSQL_DATA_DIR}"]

# PHP
RUN sed -i -e "s/;daemonize\s*=\s*yes/daemonize = no/g" /etc/php5/fpm/php-fpm.conf
RUN sed -i "s/;cgi.fix_pathinfo=1/cgi.fix_pathinfo=0/" /etc/php5/fpm/php.ini

# Loganalyzer
RUN mkdir -p /var/www
ADD ./loganalyzer-3.6.6/src/* /var/www/

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

# Nginx and supervisord
ADD ./nginx/nginx.conf /etc/nginx/nginx.conf
ADD ./supervisord.conf /etc/supervisor/conf.d/supervisord.conf



# PORTS

# Rsyslog
EXPOSE 514/tcp
EXPOSE 514/udp

# Grafana
EXPOSE 80

# Loganalyzer
EXPOSE 82

# Graphite
EXPOSE 2003

# StatsD UDP port
EXPOSE 8125/udp

# StatsD Management port
EXPOSE 8126



# COMMAND TO RUN

CMD ["/usr/bin/supervisord"]
