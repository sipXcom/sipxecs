#!/bin/bash

for i in "$@"
do
case $i in
    -s=*|--source-dir=*)
    SOURCE_DIR="${i#*=}"
    shift
    ;;
    -v=*|--version=*)
    VERSION="${i#*=}"
    shift
    ;;
    -p=*|--project=*)
    PROJECT="${i#*=}"
    shift
    ;;
    *)
        echo "Usage: -s|--source-dir: absolute path to source directory, e.g. /home/sipx/sipxecs
       -v|--version: version of sipxcom RPM to build, e.g. 15.10
       -p|--project: project to build (or sipx for building all RPMs) or init for the first run, e.g. init, sipXconfig, sipx
       
Sample:
       ./build-rpm.sh --source-dir=/home/sipx/sipxecs --version=15.08 --project=init
       ./build-rpm.sh --source-dir=/home/sipx/sipxecs --version=15.08 --project=sipx
       ./build-rpm.sh --source-dir=/home/sipx/sipxecs --version=15.08 --project=sipXconfig"
	exit
    ;;
esac
done
sudo docker run -e "SIPXCOM_VERSION=${VERSION}" --rm -t --privileged -v ${SOURCE_DIR}:/home/sipx/sipxcom dizzy/docker-dev-rpm:15.10 ${PROJECT}
