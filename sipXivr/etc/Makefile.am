include $(top_srcdir)/config/utility.am

EXTRA_DIST = \
	$(schema_DATA:=.in) \
	$(conf_DATA:=.in) \
	$(tmpfilesd_SCRIPTS:=.in)

cfinputsdir = $(SIPX_CFINPUTS)/plugin.d
dist_cfinputs_DATA = \
	sipxivr.cf

schemadir = @SIPX_DATADIR@/schema
schema_DATA = \
	autoattendants.xsd \
	validusers.xsd

confdir = @SIPX_CONFDIR@/sipxivr
conf_DATA = \
	sipxivr.sec.erb \
	log4j.properties

tmpfilesddir = /etc/tmpfiles.d
tmpfilesd_SCRIPTS = \
	sipxivr.conf

$(schema_DATA) : % : %.in Makefile
	@$(call SearchAndReplace, $<, $@)

$(conf_DATA) $(tmpfilesd_SCRIPTS) : % : %.in Makefile
	@$(call SearchAndReplace, $<, $@)

CLEANFILES = \
	$(schema_DATA) \
	$(conf_DATA) \
	$(tmpfilesd_SCRIPTS)
