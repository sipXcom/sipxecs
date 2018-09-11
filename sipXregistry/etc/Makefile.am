include $(top_srcdir)/config/utility.am

EXTRA_DIST = \
	$(tmpfilesd_SCRIPTS:=.in)

cfinputsdir = $(SIPX_CFINPUTS)/plugin.d
dist_cfinputs_DATA = \
	sipxregistrar.cf

tmpfilesddir = /etc/tmpfiles.d
tmpfilesd_SCRIPTS = \
	sipxregistrar.conf

$(tmpfilesd_SCRIPTS): % : %.in Makefile
	@$(call SearchAndReplace,$<,$@)
	
CLEANFILES = $(tmpfilesd_SCRIPTS)
