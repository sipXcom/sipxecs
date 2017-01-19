**Docker Phase 1**

_On the host CentOS 7 machine_ :

1. install docker engine (currently 1.12) and run docker build -t fatty . in the path where you have this Dockerfile

2. Make sure sshd is started on a different port than 22 eq 222. Edit /etc/ssh/sshd_conf ; stop sshd and restart it

3. Disable selinux -> edit /etc/selinux set it to disable --> reboot server

4. edit /etc/hosts wth the host and fqdn of the fat machine in my case uc.mihai.test


    >[root@localhost ~]# cat /etc/hosts

    >10.3.0.200  uc        uc.mihai.test

    >127.0.0.1   localhost localhost.localdomain localhost4 localhost4.localdomain4

    >::1         localhost localhost.localdomain localhost6 localhost6.localdomain6



5. Make sure named is stopped on the host machine


6. test ssh connectivity from another machine with ssh root@10.3.0.200 -p 222
   If it's not working run service firewalld stop on the host machine

7. run fat container with --net hosts and --hostname uc.mihai.test options. My command:

    >docker run -it  --privileged --name="FatContainer" -v /etc/sipxpbx  -v /var/log -v /usr/share/sipxecs -v /var/sipxdata --net host --hostname uc.mihai.test fatty

8. Start freeswitch container (!!!! AFTER following Fat Container setup instructions bellow) with:

    >docker run -d --name="FSContainer" --volumes-from "FatContainer" --net=host --privileged costache2mihai/dockerizedfreeswitch:minimalFS



_On Fat container_

1. edit /etc/resolv.conf  and make it point to himself

    >search mihai.test

    >nameserver 10.3.0.200

2. service sshd start ---> make sure it is started. i've seen a few times it didn't properly started

3. vi /usr/share/sipxecs/cfinputs/hostname.cf -> take out /etc/hosts entry -- it will cause sipxecs-setup to fail if not.
/etc/hosts is mount point in docker container

4. run sipxecs-setup and install server

5. login to gui start services and add users

6. start fs container with above commands

7. DON'T close the terminal where you have executed above steps and don't exit this docker container

that's all :) - now is code changing fun part



