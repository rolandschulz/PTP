PTP_DEBUG_SDM =	../org.eclipse.ptp.debug.sdm

INCLUDES = -I$(PTP_DEBUG_SDM)/include

LIBS = -L$(PTP_DEBUG_SDM) -lproxysvr -lutils

CC = mpicc

CFLAGS = -g

all: orte_server

orte_server: orte_server.o
	$(CC) -o orte_server $(CFLAGS) orte_server.o $(LIBS)

orte_server.o: orte_server.c
	$(CC) -c $(CFLAGS) orte_server.c $(INCLUDES)

clean:
	rm -f orte_server *.o
