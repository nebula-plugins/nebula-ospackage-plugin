#!/bin/sh
mkdir -p ./main
chown nobody:nobody ./main
exec setuidgid nobody ${logCommand}