FROM centos:centos6

MAINTAINER Mihai Costache <costache.mircea.mihai@gmail.com>

RUN yum update -y

# install SSHD, ssh clients and wget to download repo file

RUN yum install -y wget \
    initscripts \
    openssh-server \
    openssh-clients

# configure sshd
RUN mkdir /var/run/sshd && \
    echo 'root:admin' | chpasswd && \
    sed -i 's/PermitRootLogin without-password/PermitRootLogin yes/' /etc/ssh/sshd_config && \
    sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd && \
    /usr/bin/ssh-keygen -q -t rsa -f /etc/ssh/ssh_host_rsa_key -C '' -N '' && \
    /usr/bin/ssh-keygen -q -t dsa -f /etc/ssh/ssh_host_dsa_key -C '' -N ''

# install sipxcom
RUN wget -P /etc/yum.repos.d http://download.sipxcom.org/pub/sipXecs/16.08/sipxecs-16.08.0-centos.repo
RUN yum install -y sipxecs

# remove freeswitch - it will be used in a different container

RUN rpm -e $(rpm -qa 'freeswitch*') --nodeps && \
    yum clean all && yum clean metadata




ENTRYPOINT ["/bin/bash"]