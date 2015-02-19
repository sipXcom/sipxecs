include $(top_srcdir)/config/utility.am
include  $(top_srcdir)/config/java.am

EXTRA_DIST = \
	$(sipxcom_RESOURCES)

sipxcom_RESOURCES = $(shell cd $(srcdir); find . -type f \(\
	  -name '*.txt' \
	  -o -name '*.png' \
	  -o -name '*.xml' \
	  -o -name '*.properties' \
	\))

JAR_FILE = sipXcom.jar
jardir = @SIPX_JAVADIR@/sipXconfig/plugins
jar_DATA = $(JAR_FILE)

$(JAR_FILE) : $(sipxcom_RESOURCES)
	jar -cf $@.tmp .
	cd $(srcdir); jar -uf $(abspath .)/$@.tmp $(sipxcom_RESOURCES)
	mv $@.tmp $@
