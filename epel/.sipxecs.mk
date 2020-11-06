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
	l/libargon2-* \
	p/portaudio-devel-* \
	p/php73-* \
	p/php73-php-fpm-* \
	j/jack-audio-connection-kit-devel*

# Technincally these could be build and runtime requirements
RUNTIME_EPEL = \
                b/broadvoice-0.* \
		c/certbot-0.39.* \
		c/codec2-0.8.1* \
		e/elasticsearch-* \
		f/freeswitch-1.10.1* \
		f/freeswitch-application-abstraction-1.10.1* \
		f/freeswitch-application-avmd-1.10.1* \
		f/freeswitch-application-blacklist-1.10.1* \
		f/freeswitch-application-callcenter-1.10.1* \
		f/freeswitch-application-cidlookup-1.10.1* \
		f/freeswitch-application-conference-1.10.1* \
		f/freeswitch-application-curl-1.10.1* \
		f/freeswitch-application-db-1.10.1* \
		f/freeswitch-application-directory-1.10.1* \
		f/freeswitch-application-distributor-1.10.1* \
		f/freeswitch-application-easyroute-1.10.1* \
		f/freeswitch-application-enum-1.10.1* \
		f/freeswitch-application-esf-1.10.1* \
		f/freeswitch-application-expr-1.10.1* \
		f/freeswitch-application-fifo-1.10.1* \
		f/freeswitch-application-fsk-1.10.1* \
		f/freeswitch-application-fsv-1.10.1* \
		f/freeswitch-application-hash-1.10.1* \
		f/freeswitch-application-httapi-1.10.1* \
		f/freeswitch-application-http-cache-1.10.1* \
		f/freeswitch-application-lcr-1.10.1* \
		f/freeswitch-application-limit-1.10.1* \
		f/freeswitch-application-memcache-1.10.1* \
		f/freeswitch-application-mongo-1.10.1* \
		f/freeswitch-application-nibblebill-1.10.1* \
		f/freeswitch-application-rad_auth-1.10.1* \
		f/freeswitch-application-redis-1.10.1* \
		f/freeswitch-application-rss-1.10.1* \
		f/freeswitch-application-sms-1.10.1* \
		f/freeswitch-application-snapshot-1.10.1* \
		f/freeswitch-application-snom-1.10.1* \
		f/freeswitch-application-soundtouch-1.10.1* \
		f/freeswitch-application-spy-1.10.1* \
		f/freeswitch-application-stress-1.10.1* \
		f/freeswitch-application-translate-1.10.1* \
		f/freeswitch-application-valet_parking-1.10.1* \
		f/freeswitch-application-video_filter-1.10.1* \
		f/freeswitch-application-voicemail-1.10.1* \
		f/freeswitch-application-voicemail-ivr-1.10.1* \
		f/freeswitch-asrtts-flite-1.10.1* \
		f/freeswitch-asrtts-pocketsphinx-1.10.1* \
		f/freeswitch-asrtts-tts-commandline-1.10.1* \
		f/freeswitch-asrtts-unimrcp-1.10.1* \
		f/freeswitch-codec-bv-1.10.1* \
		f/freeswitch-codec-codec2-1.10.1* \
		f/freeswitch-codec-h26x-1.10.1* \
		f/freeswitch-codec-ilbc-1.10.1* \
		f/freeswitch-codec-isac-1.10.1* \
		f/freeswitch-codec-mp4v-1.10.1* \
		f/freeswitch-codec-opus-1.10.1* \
		f/freeswitch-codec-passthru-amr-1.10.1* \
		f/freeswitch-codec-passthru-amrwb-1.10.1* \
		f/freeswitch-codec-passthru-g723_1-1.10.1* \
		f/freeswitch-codec-passthru-g729-1.10.1* \
		f/freeswitch-codec-silk-1.10.1* \
		f/freeswitch-codec-siren-1.10.1* \
		f/freeswitch-codec-theora-1.10.1* \
		f/freeswitch-config-vanilla-1.10.1* \
		f/freeswitch-database-pgsql-1.10.1* \
		f/freeswitch-debuginfo-1.10.1* \
		f/freeswitch-devel-1.10.1* \
		f/freeswitch-endpoint-dingaling-1.10.1* \
		f/freeswitch-endpoint-portaudio-1.10.1* \
		f/freeswitch-endpoint-rtc-1.10.1* \
		f/freeswitch-endpoint-rtmp-1.10.1* \
		f/freeswitch-endpoint-skinny-1.10.1* \
		f/freeswitch-endpoint-verto-1.10.1* \
		f/freeswitch-event-cdr-mongodb-1.10.1* \
		f/freeswitch-event-cdr-pg-csv-1.10.1* \
		f/freeswitch-event-cdr-sqlite-1.10.1* \
		f/freeswitch-event-erlang-event-1.10.1* \
		f/freeswitch-event-format-cdr-1.10.1* \
		f/freeswitch-event-json-cdr-1.10.1* \
		f/freeswitch-event-multicast-1.10.1* \
		f/freeswitch-event-radius-cdr-1.10.1* \
		f/freeswitch-event-rayo-1.10.1* \
		f/freeswitch-event-snmp-1.10.1* \
		f/freeswitch-format-local-stream-1.10.1* \
		f/freeswitch-format-mod-shout-1.10.1* \
		f/freeswitch-format-native-file-1.10.1* \
		f/freeswitch-format-portaudio-stream-1.10.1* \
		f/freeswitch-format-shell-stream-1.10.1* \
		f/freeswitch-format-ssml-1.10.1* \
		f/freeswitch-format-tone-stream-1.10.1* \
		f/freeswitch-freetdm-1.10.1* \
		f/freeswitch-kazoo-1.10.1* \
		f/freeswitch-lang-de-1.10.1* \
		f/freeswitch-lang-en-1.10.1* \
		f/freeswitch-lang-es-1.10.1* \
		f/freeswitch-lang-fr-1.10.1* \
		f/freeswitch-lang-he-1.10.1* \
		f/freeswitch-lang-pt-1.10.1* \
		f/freeswitch-lang-ru-1.10.1* \
		f/freeswitch-lang-sv-1.10.1* \
		f/freeswitch-logger-graylog2-1.10.1* \
		f/freeswitch-lua-1.10.1* \
		f/freeswitch-perl-1.10.1* \
		f/freeswitch-python-1.10.1* \
		f/freeswitch-application-signalwire-1.10.1* \
		f/freeswitch-sounds-en-us-callie-1.0.51* \
		f/freeswitch-sounds-en-us-callie-16000-1.0.51* \
		f/freeswitch-sounds-en-us-callie-32000-1.0.51* \
		f/freeswitch-sounds-en-us-callie-48000-1.0.51* \
		f/freeswitch-sounds-en-us-callie-8000-1.0.51* \
		f/freeswitch-sounds-en-us-callie-all-1.0.51* \
		f/freeswitch-timer-posix-1.10.1* \
		f/freeswitch-xml-cdr-1.10.1* \
		f/freeswitch-xml-curl-1.10.1* \
		m/mongodb-org-3.6.* \
		m/mongodb-org-mongos-3.6.* \
		m/mongodb-org-server-3.6.* \
		m/mongodb-org-shell-3.6.* \
		m/mongodb-org-tools-3.6.* \
		e/erlang-erlando-* \
		e/erlang-ibrowse-* \
		e/erlang-rpm-macros-* \
		f/flite-2.0.0-1* \
                f/flite-debuginfo-2.0.0-1* \
                f/flite-devel-2.0.0-1* \
		f/fail2ban-* \
		g/g722_1-* \
		g/gtest-1* \
		i/ilbc-* \
		i/ilbc2-* \
		j/js-* \
		j/jemalloc-* \
		l/lame-3* \
		l/lame-libs-3.100* \
		l/libcodec2-* \
		l/libev-4.* \
		l/libdb4-* \
		l/libmcrypt-* \
		l/libffado-* \
		l/libsilk-* \
		l/libxml++-* \
		l/libargon2-* \
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
		p/python-ndg_httpsclient-* \
		p/python-pymongo-2.5* \
		p/python-requests-toolbelt-* \
		p/python-zope-component-* \
		p/python-zope-event-* \
		p/python2-acme-0.39.* \
		p/python2-certbot-0.39.* \
		p/python2-configargparse-* \
		p/python2-distro-* \
		p/python2-future-* \
		p/python2-josepy-* \
		p/python2-mock-* \
		p/python2-parsedatetime-* \
		p/python2-pyrfc3339-* \
		p/python2-six-* \
		p/python2-rpm-macros-* \
		p/python-srpm-macros-* \
		p/python-rpm-macros-* \
		p/php73-* \
		p/php73-php-fpm-* \
		r/redis-* \
		r/rubygem-daemons-* \
		r/rubygem-pg-* \
		s/sec-* \
		s/signalwire-client-* \
		s/shorewall-5* \
		s/shorewall-core-* \
		v/v8-3* \
		w/wxBase-* \
		w/wxGTK-2.* \
		w/wxGTK-gl-* \
		l/leveldb-* \
		l/libks-1.2.0* \
		p/python-pymongo-gridfs-2.5* \
		s/sipp-* \
		p/portaudio-19* \
		j/jack-audio-connection-kit-*

