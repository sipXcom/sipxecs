include config/utility.am
include config/project.am
################################
### FreeSwitch headers files found in libfreeswitch-dev ###
FS_INCLUDES=/usr/include/freeswitch
FS_MODULES=/usr/lib64/freeswitch/mod
################################

EXTRA_DIST = src

### END OF CUSTOMIZATION ###
SHELL := /bin/bash

CC=gcc 
CFLAGS=-fPIC -O3 -fomit-frame-pointer -fno-exceptions -Wall -std=c99 -pedantic -m64 -mtune=generic

INCLUDES=-I/usr/include -Ibcg729/include -I$(FS_INCLUDES)
LDFLAGS=-lm -Wl,-static -Lbcg729/src/.libs -lbcg729 -Wl,-Bdynamic 

all : mod_bcg729.o
	$(CC) $(CFLAGS) $(INCLUDES) -shared -Xlinker -x -o mod_bcg729.so mod_bcg729.o $(LDFLAGS)

mod_bcg729.o:
	$(CC) $(CFLAGS) $(INCLUDES) -c src/mod_bcg729.c
	
clean:
	rm -f *.o *.so *.a *.la

install: all
	/usr/bin/install -c mod_bcg729.so $(INSTALL_PREFIX)/$(FS_MODULES)/mod_bcg729.so
