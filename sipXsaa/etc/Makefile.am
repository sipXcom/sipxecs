include $(top_srcdir)/config/utility.am

EXTRA_DIST = \
	sample-appearance-groups.xml \
	$(tmpfilesd_SCRIPTS:=.in)

cfinputsdir = $(SIPX_CFINPUTS)/plugin.d
dist_cfinputs_DATA = \
	sipxsaa.cf

tmpfilesddir = /etc/tmpfiles.d
tmpfilesd_SCRIPTS = \
	sipxsaa.conf

$(tmpfilesd_SCRIPTS) : % : %.in Makefile
	@$(call SearchAndReplace,$<,$@)

CLEANFILES = $(tmpfilesd_SCRIPTS)
