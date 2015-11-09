include $(top_srcdir)/config/utility.am

EXTRA_DIST =

cfinputsdir = $(SIPX_CFINPUTS)/plugin.d
dist_cfinputs_DATA = \
	sipxtcpdumplog.cf

$(proc_DATA) : % : %.in Makefile
	@$(call SearchAndReplace, $<, $@)

CLEANFILES =
