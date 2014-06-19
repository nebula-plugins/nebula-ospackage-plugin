#!/bin/sh
# chkconfig: ${runLevels.join("")} ${startSequence} ${stopSequence}
# description: Control Script for ${daemonName}

. /etc/rc.d/init.d/functions

service=${daemonName}
subsys_lock="/var/lock/subsys/$service"
daemontools_service=/service/${daemonName}

if [ -e /etc/sysconfig/${daemonName} ]; then
  . /etc/sysconfig/${daemonName}
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

case \$1 in

start)
    echo -n "$service: "
    start
    if [ $? -eq 0 ]
    then
        success
        touch $subsys_lock
    else
        failure
    fi
    echo
    ;;
 stop)
    echo -n "$service:  "
    stop
    if [ $? -eq 0 ]
    then
        success
        rm -f $subsys_lock
    else
        failure
    fi
    echo
    ;;
restart)
    echo -n "$service: "
    restart
    if [ $? -eq 0 ]
    then
        success
        touch $subsys_lock
    else
        failure
    fi
    echo
    ;;
  *)
    echo "Usage: service $service {start|stop|restart}"
    exit 1
esac