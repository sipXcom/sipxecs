mongo-cxx-driver_VER = 2.6.7
mongo-cxx-driver_REL = 4
mongo-cxx-driver_SRPM = mongo-cxx-driver-$(mongo-cxx-driver_VER)-$(mongo-cxx-driver_REL)$(RPM_DIST).src.rpm
mongo-cxx-driver_SPEC = $(SRC)/$(PROJ)/mongo-cxx-driver.spec
mongo-cxx-driver_SOURCES = \
        mongo-cxx-driver-legacy-0.0-26compat-2.6.7.tar.gz \
	$(SRC)/$(PROJ)/mongo-cxx-driver-2.6.7-logger.patch \
	$(SRC)/$(PROJ)/mongo-cxx-driver-2.6.7-maxTimeMS.patch \
	$(SRC)/$(PROJ)/mongo-cxx-driver-2.6.7-UC-4104.patch
	                                                                                                                                     

mongo-cxx-driver.dist:;
