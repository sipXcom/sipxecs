include $(top_srcdir)/config/utility.am

EXTRA_DIST = \
	sample-resource-lists.xml \
	$(tmpfilesd_SCRIPTS:=.in)

cfinputsdir = $(SIPX_CFINPUTS)/plugin.d
dist_cfinputs_DATA = \
	sipxrls.cf

tmpfilesddir = /etc/tmpfiles.d
tmpfilesd_SCRIPTS = \
	sipxrls.conf

$(tmpfilesd_SCRIPTS) : % : %.in Makefile
	@$(call SearchAndReplace,$<,$@)

CLEANFILES = $(tmpfilesd_SCRIPTS)
