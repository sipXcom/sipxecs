FROM centos:6.8
MAINTAINER Mihai <costache.mircea.mihai@gmail.com>
ADD sipxcom.repo /etc/yum.repos.d/
RUN yum install -y sipxconfig sipxsupervisor
VOLUME ["/etc/sipxpbx/", "/var/sipxdata/tmp/", "/var/log/sipxpbx/", "/var/run/sipxpbx/","/usr/share/java/sipXecs/"]
RUN rm -rf /etc/yum.repos.d/sipxcom.repo && yum clean all && rm -rf /var/cache/yum/x86_64/*
CMD /etc/init.d/sipxconfig nofork
#TBD
