AC_PREREQ(2.57)
AC_INIT(sipXcom, 15.06, sipx-dev@list.sipxcom.org)
AC_CONFIG_AUX_DIR(config)
AM_INIT_AUTOMAKE(foreign tar-ustar)
m4_include([config/general.m4])
m4_include([config/sipXlib.m4])
m4_include([config/sipXlib2.m4])
m4_include([config/java.m4])
m4_include([config/java2.m4])

AC_PROG_JAVA([java])
PROG_JAVA_DEP
# Psotgres operations
CHECK_POSTGRES

AC_CONFIG_FILES([
    Makefile
    src/Makefile
])
AC_OUTPUT
