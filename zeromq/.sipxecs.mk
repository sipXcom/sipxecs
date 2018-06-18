zeromq_VER = 2.2.0
zeromq_REL = 1
zeromq_SRPM = zeromq-$(zeromq_VER)-$(zeromq_REL)$(RPM_DIST).src.rpm
zeromq_SPEC = $(SRC)/$(PROJ)/zeromq.spec
zeromq_SOURCES = zeromq-2.2.0.tar.gz
zeromq.dist :;
