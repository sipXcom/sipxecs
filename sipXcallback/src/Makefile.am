include $(top_srcdir)/config/utility.am
include $(top_srcdir)/config/java.am
include $(top_srcdir)/common.am

EXTRA_DIST = \
	$(sipxcall_SRC) \
	$(sipxcall_RESOURCES)

noinst_DATA = javac-sipxcall

sipxcall_SRC = \
	$(shell cd $(srcdir); find org -type f -name '*.java')
sipxcall_RESOURCES = $(shell cd $(srcdir); find . \( -name '*.xml' \) )

sipxcall_DEPS = \
	$(JAVAROOT) \
	$(call JavaDep, @SIPX_JAVADIR@/sipXcommons, $(sipxcall_PKGS))

jardir = @SIPX_JAVADIR@/sipXcallback
JAR_FILE = sipXcallback.jar
jar_DATA = $(JAR_FILE)

$(JAR_FILE) : javac-sipxcall $(sipxcall_RESOURCES) Manifest.txt Makefile
	jar cfm $@ Manifest.txt \
	  $(call JarInclude,$(sipxcall_JAVAROOT),.) \
	  $(call JarInclude,$(srcdir),$(sipxcall_RESOURCES))
