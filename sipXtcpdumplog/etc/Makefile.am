include $(top_srcdir)/config/utility.am

EXTRA_DIST = $(tmpfilesd_SCRIPTS:=.in)

cfinputsdir = $(SIPX_CFINPUTS)/plugin.d
dist_cfinputs_DATA = \
	sipxtcpdumplog.cf

tmpfilesddir = /etc/tmpfiles.d
tmpfilesd_SCRIPTS = \
	sipxtcpdumplog.conf

$(proc_DATA) $(tmpfilesd_SCRIPTS) : % : %.in Makefile
	@$(call SearchAndReplace, $<, $@)

CLEANFILES = $(tmpfilesd_SCRIPTS)
