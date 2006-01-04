PTP_DEBUG_SDM =	../org.eclipse.ptp.debug.sdm

INCLUDES = -I$(PTP_DEBUG_SDM)/include

LIBS = -L$(PTP_DEBUG_SDM) -lproxysvr -lutils

CC = mpicc

CFLAGS = -g

ARCH := $(shell uname -p | sed -e 's/powerpc/ppc/' -e 's/ppc64/ppc/' -e 's/i.86/x86/')
OS := $(shell uname -s | sed -e 's/Darwin/macosx/' -e 's/Linux/linux/')

INSTALL_DIR = ../org.eclipse.ptp.orte.$(OS).$(ARCH)/bin

all: orte_server

orte_server: orte_server.o
	$(CC) -o orte_server $(CFLAGS) orte_server.o $(LIBS)

orte_server.o: orte_server.c
	$(CC) -c $(CFLAGS) orte_server.c $(INCLUDES)

install:
	cp orte_server $(INSTALL_DIR)
	chmod 755 $(INSTALL_DIR)/orte_server
	
clean:
	rm -f orte_server *.o
