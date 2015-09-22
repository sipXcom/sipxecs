include $(top_srcdir)/config/utility.am
include $(top_srcdir)/config/java.am
include $(srcdir)/common.am

EXTRA_DIST = \
	$(phoneplugin_SRC) \
	$(phoneplugin_RESOURCES)

noinst_DATA = javac-phoneplugin

jardir = @SIPX_JAVADIR@/sipXconfig/plugins
JAR_FILE = sipXjitsiphoneplugin.jar
jar_DATA = $(JAR_FILE)

phoneplugin_SRC = $(shell cd $(srcdir); find org -type f -name '*.java')
phoneplugin_RESOURCES = sipxplugin.beans.xml

phoneplugin_DEPS = \
	$(call JavaDep,@SIPX_JAVADIR@/sipXconfig @SIPX_JAVADIR@/sipXcommons,$(phoneplugin_PKGS))

$(JAR_FILE) : javac-phoneplugin $(phoneplugin_RESOURCES)
	jar cf $@ \
	  $(call JarInclude,$(JAVAROOT),.) \
	  $(call JarInclude,$(srcdir),$(phoneplugin_RESOURCES))
