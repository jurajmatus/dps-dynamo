#!/bin/bash
mkdir -p "$MYSQL_DATA_DIR"
chown -R mysql:mysql "$MYSQL_DATA_DIR"
echo 'Initializing database'
mysqld --initialize-insecure=on --user=mysql --datadir="$MYSQL_DATA_DIR"
echo 'Database initialized'

mysqld --user=mysql --datadir="$DATADIR" --skip-networking &
pid="$!"

mysql=( mysql --protocol=socket -uroot )

mysql_tzinfo_to_sql /usr/share/zoneinfo | "${mysql[@]}" mysql

"${mysql[@]}" <<-EOSQL
   -- What's done in this file shouldn't be replicated
   --  or products like mysql-fabric won't work
   SET @@SESSION.SQL_LOG_BIN=0;
   DELETE FROM mysql.user where user != 'mysql.sys';
   CREATE USER 'root'@'%' IDENTIFIED BY '${MYSQL_ROOT_PASSWORD}' ;
   GRANT ALL ON *.* TO 'root'@'%' WITH GRANT OPTION ;
   DROP DATABASE IF EXISTS test ;
   FLUSH PRIVILEGES ;
EOSQL

mysql+=( -p"${MYSQL_ROOT_PASSWORD}" )

if [ "$MYSQL_DATABASE" ]; then
   echo "CREATE DATABASE IF NOT EXISTS \`$MYSQL_DATABASE\` ;" | "${mysql[@]}"
   mysql+=( "$MYSQL_DATABASE" )
fi

if [ "$MYSQL_USER" -a "$MYSQL_PASSWORD" ]; then
   echo "CREATE USER '"$MYSQL_USER"'@'%' IDENTIFIED BY '"$MYSQL_PASSWORD"' ;" | "${mysql[@]}"

   if [ "$MYSQL_DATABASE" ]; then
      echo "GRANT ALL ON \`"$MYSQL_DATABASE"\`.* TO '"$MYSQL_USER"'@'%' ;" | "${mysql[@]}"
   fi

   echo 'FLUSH PRIVILEGES ;' | "${mysql[@]}"
fi

echo
echo 'MySQL init process done. Ready for start up.'
echo

chown -R mysql:mysql "$MYSQL_DATA_DIR"

mysqld > /dev/null 2>&1 & disown
