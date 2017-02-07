FROM centos:centos6
MAINTAINER Mihai Costache <costache.mircea.mihai@gmail.com>
ADD sipxcom.repo /etc/yum.repos.d/
EXPOSE 5432
RUN useradd  postgres
RUN yum install -y postgresql-libs-8.4* postgresql-server-8.4* postgresql-8.4* ruby-postgres-0.7* && \
     rm -rf /etc/yum.repos.d/sipxecs.repo && \
     yum clean all && \
     rm -rf /var/cache/yum/*
RUN mkdir -p /var/lib/pgsql && chown -R postgres:postgres /var/lib/pgsql

USER root
RUN service postgresql initdb

USER postgres


CMD /usr/bin/postmaster -p 5432 -D /var/lib/pgsql/data
