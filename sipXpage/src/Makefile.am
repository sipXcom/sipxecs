include $(top_srcdir)/config/utility.am
include $(top_srcdir)/config/java.am
include $(top_srcdir)/common.am

EXTRA_DIST = \
	$(page_SRC)
	$(page_RESOURCES)

noinst_DATA = javac-page

jardir = @SIPX_JAVADIR@/sipXpage
JAR_FILE = sipXpage.jar
jar_DATA = $(JAR_FILE)

page_SRC = $(shell cd $(srcdir); find org -type f -name '*.java')
page_RESOURCES = sipxplugin.beans.xml

page_DEPS = \
	$(call JavaDep,@SIPX_JAVADIR@/sipXcommons,$(page_PKGS))

$(JAR_FILE) : javac-page $(page_RESOURCES)
	jar cf $@ \
	  $(call JarInclude,$(JAVAROOT),.) \
	  $(call JarInclude,$(srcdir),$(page_RESOURCES))
	  
style_SRC = $(page_SRC)
precommit : style validate-xml

validate-xml : sipxplugin.beans.xml
	xmllint --noout \
	  --schema $(SIPX_DATADIR)/schema/spring-beans-2.0.xsd  \
	$^

	  