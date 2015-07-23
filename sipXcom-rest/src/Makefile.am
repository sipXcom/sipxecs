include $(top_srcdir)/config/utility.am
include  $(top_srcdir)/config/java.am
include $(top_srcdir)/common.am

EXTRA_DIST = \
	$(sipxcomrest_SRC) \
	$(sipxcomrest_RESOURCES)

noinst_DATA = javac-sipxcomrest

sipxcomrest_RESOURCES = \
	sipxplugin.beans.xml

JAR_FILE = sipxcom-rest.jar
jardir = @SIPX_JAVADIR@/sipXconfig/plugins
jar_DATA = $(JAR_FILE)

sipxcomrest_SRC = $(shell cd $(srcdir); find . -name '*.java')

sipxcomrest_DEPS = \
	$(JAVAROOT) \
	$(call JavaDep,@SIPX_JAVADIR@/sipXcommons @SIPX_JAVADIR@/sipXconfig,$(sipxcomrest_PKGS))

$(JAR_FILE) : javac-sipxcomrest $(sipxcomrest_RESOURCES)
	jar -cf $@.tmp -C $(JAVAROOT) .
	cd $(srcdir); jar -uf $(abspath .)/$@.tmp $(sipxcomrest_RESOURCES)
	mv $@.tmp $@