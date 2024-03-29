#!/bin/bash

CURRENT_DIR=`dirname $0`
API_HOME=`cd "$CURRENT_DIR/.." >/dev/null; pwd`
Jar=`ls $API_HOME/lib/*.jar`
RETVAL="0"
LOG="api_stdout.log"

# run redis
nohup /usr/bin/redis-server /etc/redis.conf &
# run nginx
cd /usr/bin
nginx

Region=$4
echo $Region
if [ ! -n "$Region" ];then
  Region="CN"
fi

# run java application
cd $API_HOME
java \
-Dconnector.ak=$1 \
-Dconnector.sk=$2 \
-Dproject.code=$3 \
-Dconnector.region=$Region \
-jar $Jar >> $API_HOME/logs/$LOG 2>&1


