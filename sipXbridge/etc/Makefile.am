include $(top_srcdir)/config/utility.am

EXTRA_DIST = \
	$(conf_DATA:=.in) \
	$(tmpfilesd_SCRIPTS:=.in)

confdir = @SIPX_CONFDIR@/sipxbridge
conf_DATA = \
	sipxbridge.sec.erb \
	log4j.properties

cfinputsdir = $(SIPX_CFINPUTS)/plugin.d
dist_cfinputs_DATA = \
	sipxbridge.cf

tmpfilesddir = /etc/tmpfiles.d
tmpfilesd_SCRIPTS = \
	sipxbridge.conf

$(conf_DATA) $(tmpfilesd_SCRIPTS) : % : %.in Makefile
	@$(call SearchAndReplace,$<,$@)

DISTCLEANFILES = $(conf_DATA) $(tmpfilesd_SCRIPTS)
