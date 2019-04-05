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
	r/rubygem-net-ssh-* \
	r/rubygem-net-sftp-* \
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
	elasticsearch-* \
	mongodb-org-server-3.6* \
	mongodb-org-shell-3.6* \
	mongodb-org-tools-3.6* \
	erlang-lager-* \
	erlang-gen_leader-* \
	erlang-gproc-* \
	erlang-erlando-* \
	erlang-ibrowse-* \
	erlang-rpm-macros-* \
	fail2ban-0.9.6* \
	gperftools-libs-* \
	gtest-1* \
	js-* \
	libiodbc-3* \
	libev-4.* \
	libmcrypt-* \
	libunwind-* \
	monit-* \
	openpgm-5* \
	php-pecl-mongo-* \
	poco-crypto-* \
	poco-data-* \
	poco-foundation-* \
	poco-mysql-* \
	poco-net-* \
	poco-netssl-* \
	poco-odbc-* \
	poco-pagecompiler-* \
	poco-sqlite-* \
	poco-util-* \
	poco-xml-* \
	poco-zip-* \
	python-argparse-* \
	python-bson-2.5* \
	python-inotify-0* \
	python-pymongo-2.5* \
	redis-* \
	rubygem-daemons-* \
	sec-* \
	shorewall-4* \
	shorewall-core-* \
	snappy-* \
	v8-3* \
	wxBase-* \
	wxGTK-2.* \
	wxGTK-gl-* \
	zeromq-* \
	leveldb-* \
	python-pymongo-gridfs-2.5* \
	sipp-* \
	socat-1.7.* \
	ldns-1.6.16-2* \
	portaudio-19* \
	jack-audio-connection-kit-*

