# **DockerizedPostgres8.4.20**

This docker images is used to run a postgres container in the sipxcom/uniteme dockerized project
Since official postgres repository on dockerhub does not support this version we needed to create an postgres image based on RPM's

To use this image you will need to have installed a centos 7 virtual or physical server, sipxcom or uniteme "Fat" container and mongodb container

This image will run with privileged access to hosts network and with shared volumes wih the other containers

/var/lib/pgsql/ needs to be created on host machine and correct "postgres" owner should be associated

Locally build image named psql can be started as:
docker run -d --net=host --privileged -v /var/lib/psql:/var/lib/psql --name psql psql