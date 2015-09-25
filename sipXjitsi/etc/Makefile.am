include $(top_srcdir)/config/utility.am

cfgdir = $(SIPX_CONFDIR)/jitsi
dist_cfg_DATA = \
	jitsi/line.properties \
	jitsi/line.xml \
	jitsi/jitsi.properties.vm \
	jitsi/phone.properties \
	jitsi/phone.xml \
	jitsi/types.xml

cfg_DATA = \
	jitsiconf.properties	

$(cfg_DATA) : % : %.in Makefile
	@$(call SearchAndReplace,$<,$@)
	
EXTRA_DIST = \
	$(cfg_DATA:=.in)
	
CLEANFILES = \
	$(cfg_DATA)		

$(emptydirs) :
	$(INSTALL) -d $@

install-data-hook : $(emptydirs)