include $(top_srcdir)/config/utility.am

cfinputsdir = $(SIPX_CFINPUTS)/plugin.d
dist_cfinputs_DATA = \
	sipxcallback.cf

schemadir = @SIPX_DATADIR@/schema

EXTRA_DIST = \
	$(conf_DATA:=.in)

confdir = @SIPX_CONFDIR@/sipxcallback
conf_DATA = \
	log4j.properties

$(conf_DATA) : % : %.in Makefile
	@$(call SearchAndReplace,$<,$@)
