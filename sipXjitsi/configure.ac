AC_PREREQ(2.57)
AC_INIT(sipXjitsi, 15.12, sipx-dev@list.sipxcom.org)
AC_CONFIG_AUX_DIR(config)
m4_include([config/general.m4])
m4_include([config/sipXlib.m4])
m4_include([config/java2.m4])
AM_INIT_AUTOMAKE(foreign tar-ustar)
SFAC_AUTOMAKE_VERSION([1.6])
SFAC_INIT_FLAGS
AC_PROG_JAVA_CC([javac])
AC_PROG_JAVA([java])
PROG_JAVA_DEP
AC_CONFIG_FILES([
  Makefile 
  webapp/Makefile
  webapp/src/Makefile
  phoneplugin/Makefile
  phoneplugin/src/Makefile
  etc/Makefile
])
AC_OUTPUT