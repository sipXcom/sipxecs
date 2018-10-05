include $(top_srcdir)/config/utility.am

cfinputsdir = $(SIPX_CFINPUTS)/plugin.d
dist_cfinputs_DATA = \
	sipxcallback.cf

schemadir = @SIPX_DATADIR@/schema

EXTRA_DIST = \
	$(conf_DATA:=.in) \
	$(tmpfilesd_SCRIPTS:=.in)

confdir = @SIPX_CONFDIR@/sipxcallback
conf_DATA = \
	log4j.properties

tmpfilesddir = /etc/tmpfiles.d
tmpfilesd_SCRIPTS = \
	sipxcallback.conf

$(conf_DATA) $(tmpfilesd_SCRIPTS) : % : %.in Makefile
	@$(call SearchAndReplace,$<,$@)

CLEANFILES = $(conf_DATA) $(tmpfilesd_SCRIPTS)
