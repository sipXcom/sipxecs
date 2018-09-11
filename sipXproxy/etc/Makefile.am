include $(top_srcdir)/config/utility.am

EXTRA_DIST = \
	$(conf_DATA:=.in) \
	$(tmpfilesd_SCRIPTS:=.in)

cfinputsdir = $(SIPX_CFINPUTS)/plugin.d
dist_cfinputs_DATA = \
	sipxproxy.cf

confdir = $(SIPX_CONFDIR)/sipxproxy
conf_DATA = \
	sipxproxy.sec.erb
	
tmpfilesddir = /etc/tmpfiles.d
tmpfilesd_SCRIPTS = \
	sipxproxy.conf

$(conf_DATA) $(tmpfilesd_SCRIPTS) : % : %.in Makefile
	@$(call SearchAndReplace,$<,$@)

CLEANFILES = $(conf_DATA) $(tmpfilesd_SCRIPTS)
