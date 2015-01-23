#!/bin/sh
export PATH=$PATH:/usr/lib/postgresql/9.1/bin
psql -h /home/$1/tmp/postgresql oskaridb
