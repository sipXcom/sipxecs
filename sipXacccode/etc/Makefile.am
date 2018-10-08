include $(top_srcdir)/config/utility.am

cfinputsdir = $(SIPX_CFINPUTS)/plugin.d
dist_cfinputs_DATA = \
	sipxacccode.cf

schemadir = @SIPX_DATADIR@/schema
dist_schema_DATA = \
	authcodes.xsd

EXTRA_DIST = \
	$(conf_DATA:=.in) \
	$(tmpfilesd_SCRIPTS:=.in)

confdir = @SIPX_CONFDIR@/sipxacccode
conf_DATA = \
	log4j.properties

tmpfilesddir = /etc/tmpfiles.d
tmpfilesd_SCRIPTS = \
	sipxacccode.conf

$(conf_DATA) $(tmpfilesd_SCRIPTS) : % : %.in Makefile
	@$(call SearchAndReplace,$<,$@)

CLEANFILES = $(conf_DATA) $(tmpfilesd_SCRIPTS)
