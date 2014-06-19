#!/bin/sh
### BEGIN INIT INFO
# Provides:          ${daemonName}
# Default-Start:     ${runLevels}.join(" ")
# Default-Stop:      0 1 6
# Required-Start:
# Required-Stop:
# Description: Control Script for ${daemonName}
### END INIT INFO

service=${daemonName}
daemontools_service=/service/${daemonName}

if [ -e /etc/default/${daemonName} ]; then
  . /etc/default/${daemonName}
fi

stop() {
    touch $daemontools_service/down
    svc -d $daemontools_service
}

start() {
    rm -f $daemontools_service/down
    svc -u $daemontools_service
}

restart() {
    svc -t $daemontools_service
}

case $1 in

start)
    echo -n "${service}: "
    start
    if [ $? -eq 0 ]
    then
        echo "OK"
    else
        echo "ERROR"
    fi
    ;;
 stop)
    echo -n "$service:  "
    stop
    if [ $? -eq 0 ]
    then
        echo "OK"
    else
        echo "ERROR"
    fi
    ;;
restart)
    echo -n "${service}: "
    restart
    if [ $? -eq 0 ]
    then
        echo "OK"
    else
        echo "ERROR"
    fi
    ;;
  *)
    echo "Usage: service ${service} {start|stop|restart}"
    exit 1
esac