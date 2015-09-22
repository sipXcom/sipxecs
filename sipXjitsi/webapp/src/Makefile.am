include $(top_srcdir)/config/utility.am
include $(top_srcdir)/config/java.am
include $(top_srcdir)/common.am

EXTRA_DIST = \
	$(sipxjitsi_SRC) \
	$(war_RESOURCES)

war_RESOURCES = $(shell cd $(srcdir); find context -type f)

noinst_DATA = javac-sipxjitsi

sipxjitsi_SRC = $(shell cd $(srcdir); find . -name '*.java')

WAR_FILE = jitsiprov.war
wardir = @SIPX_JAVADIR@/sipXconfig
war_DATA = $(WAR_FILE)

sipxjitsi_DEPS = \
	$(call JavaDep,@SIPX_JAVADIR@/sipXconfig @SIPX_JAVADIR@/sipXcommons,$(sipxjitsi_PKGS))

sipxjitsi_JAVAROOT = out/WEB-INF/classes

$(WAR_FILE) : javac-sipxjitsi
	jar -cf $@.tmp \
	  $(call JarInclude,out,.)
	jar -uf $@.tmp \
	  $(call JarInclude,$(srcdir)/context,.)
	mv $@.tmp $@
