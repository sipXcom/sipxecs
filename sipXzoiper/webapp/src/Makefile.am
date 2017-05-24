include $(top_srcdir)/config/utility.am
include $(top_srcdir)/config/java.am
include $(top_srcdir)/common.am

EXTRA_DIST = \
	$(sipxzoiper_SRC) \
	$(war_RESOURCES)

war_RESOURCES = $(shell cd $(srcdir); find context -type f)

noinst_DATA = javac-sipxzoiper

sipxzoiper_SRC = $(shell cd $(srcdir); find . -name '*.java')

WAR_FILE = sipxZoiperProv.war
wardir = @SIPX_JAVADIR@/sipXconfig
war_DATA = $(WAR_FILE)

sipxzoiper_DEPS = \
	$(call JavaDep,@SIPX_JAVADIR@/sipXconfig @SIPX_JAVADIR@/sipXcommons,$(sipxzopier_PKGS))

sipxzoiper_JAVAROOT = out/WEB-INF/classes

$(WAR_FILE) : javac-sipxzoiper
	jar -cf $@.tmp \
	  $(call JarInclude,out,.)
	jar -uf $@.tmp \
	  $(call JarInclude,$(srcdir)/context,.)
	mv $@.tmp $@