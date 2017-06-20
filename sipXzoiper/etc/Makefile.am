include $(top_srcdir)/config/utility.am

cfgdir = $(SIPX_CONFDIR)/zoiper
dist_cfg_DATA = \
	zoiper/provisioning.xml.vm \
	zoiper/phonebook.xml.vm \
    zoiper/line.properties \
	zoiper/line.xml \
	zoiper/phone.properties \
	zoiper/phone.xml \
	zoiper/types.xml

cfg_DATA = \
	zoiperconf.properties    

$(cfg_DATA) : % : %.in Makefile
	@$(call SearchAndReplace,$<,$@)
	
EXTRA_DIST = \
	$(cfg_DATA:=.in)
	
CLEANFILES = \
	$(cfg_DATA)		

$(emptydirs) :
	$(INSTALL) -d $@

install-data-hook : $(emptydirs)