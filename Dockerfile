FROM centos:6.8
MAINTAINER Roman Romanchenko <rromanchenko@ezuce.com>

ADD sipxcom.repo /etc/yum.repos.d/
RUN yum install -y sipxcom && rm -rf /etc/yum.repos.d/sipxecs.repo && yum clean all && rm -rf /var/cache/yum/x86_64/6/sipxecs
