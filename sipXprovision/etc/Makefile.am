include $(top_srcdir)/config/utility.am

EXTRA_DIST = $(tmpfilesd_SCRIPTS:=.in)

cfinputsdir = @SIPX_CFINPUTS@/plugin.d
dist_cfinputs_DATA = \
	sipxprovision.cf

cfgdir = @SIPX_CONFDIR@/sipxprovision
dist_cfg_DATA = \
	sipxprovision.xml \
	sipxprovision.properties

tmpfilesddir = /etc/tmpfiles.d
tmpfilesd_SCRIPTS = \
	sipxprovision.conf

$(tmpfilesd_SCRIPTS) : % : %.in Makefile
	@$(call SearchAndReplace,$<,$@)

CLEANFILES = $(tmpfilesd_SCRIPTS)
