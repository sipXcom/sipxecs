# Initial Version Copyright (C) 2013 eZuce, Inc., All Rights Reserved.
# Licensed to the User under the LGPL license.

# EPEL changes are more dramatic then CentOS base. Major new versions are introduced
# at any given moment and when it happens, sipXecs has to rush out new support.
#   See http://track.sipfoundry.org/browse/UC-1664
# By bundling EPEL rpms into sipXecs, we're more in control of when rpms upgrade but
# we need to make it easy to stay somewhat in sync w/EPEL.

ifeq ($(DISTRO_ARCH),x86_64)
EXCLUDE_ARCH='--exclude=*.i686.rpm'
endif

epel.dist epel.srpm:;

epel.rpm :
	rsync -av \
	  $(EXCLUDE_ARCH) \
	  $(addprefix $(CENTOS_RSYNC_URL)/epel7/7/$(DISTRO_ARCH)/Packages/,$(RUNTIME_EPEL) $(BUILD_EPEL)) \
	  $(MOCK_RESULTS_DIR)/
	mock $(MOCK_OPTS) --scrub=cache
	$(MAKE) repo-dedup
	cd $(MOCK_RESULTS_DIR); createrepo $(CREATEREPO_OPTS) .
	$(SRC)/tools/dep save $(DISTRO).$(PROJ).rpm $(SRC)/$(PROJ)

epel.clean :
	-rm $(addprefix $(MOCK_RESULTS_DIR)/,$(BUILD_EPEL) $(RUNTIME_EPEL))

# Only used for building, not required for ISO
BUILD_EPEL = \
	b/bakefile-* \
	c/Canna-libs-* \
	c/ccache-* \
	c/compface-1.5*\
	e/erlang-* \
	g/gtest-devel-* \
	g/gyp-* \
	l/libmcrypt-devel-* \
	l/libev-* \
	l/libuv-* \
	l/libuv-devel-* \
	n/neXtaw-* \
	n/node-gyp-* \
	n/nodejs-* \
	n/npm-* \
	p/poco-* \
	p/poco-debug-* \
	p/python-empy-* \
	r/rubygem-mocha-* \
	v/v8-* \
	v/v8-devel-* \
	w/wxGTK-devel-* \
	w/wxGTK-media-* \
	x/xemacs-* \
	l/leveldb-devel-* \
	p/portaudio-devel-* \
	j/jack-audio-connection-kit-devel*

# Technincally these could be build and runtime requirements
RUNTIME_EPEL = \
	e/elasticsearch-* \
	f/freeswitch-* \
	m/mongodb-org-* \
	e/erlang-erlando-* \
	e/erlang-ibrowse-* \
	e/erlang-rpm-macros-* \
	f/fail2ban-* \
	g/gtest-1* \
	j/js-* \
	l/libev-4.* \
	l/libmcrypt-* \
	m/monit-* \
	o/openpgm-5* \
	p/php-pecl-mongo-* \
	p/poco-crypto-* \
	p/poco-data-* \
	p/poco-foundation-* \
	p/poco-mysql-* \
	p/poco-net-* \
	p/poco-netssl-* \
	p/poco-odbc-* \
	p/poco-pagecompiler-* \
	p/poco-sqlite-* \
	p/poco-util-* \
	p/poco-xml-* \
	p/poco-zip-* \
	p/python-bson-2.5* \
	p/python-pymongo-2.5* \
	r/redis-* \
	r/rubygem-daemons-* \
	s/sec-* \
	s/shorewall-5* \
	s/shorewall-core-* \
	v/v8-3* \
	w/wxBase-* \
	w/wxGTK-2.* \
	w/wxGTK-gl-* \
	l/leveldb-* \
	p/python-pymongo-gridfs-2.5* \
	s/sipp-* \
	p/portaudio-19* \
	j/jack-audio-connection-kit-*

