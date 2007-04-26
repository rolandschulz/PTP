#
# Makes a replacement LSF lsb_* batch library for testing
#

CC = mpicc

PROXY_OBJ = ../../org.eclipse.ptp.orte.proxy/ptp_orte_proxy-ptp_orte_proxy.o

UTIL_PATH = ../../org.eclipse.ptp.utils

PROXY_SVR_PATH = ../../org.eclipse.ptp.proxy

all: ptp_orte_proxy

ptp_orte_proxy: $(PROXY_OBJ)
	$(CC) -o ptp_orte_proxy $(PROXY_OBJ) $(PROXY_SVR_PATH)/libproxysvr.a $(UTIL_PATH)/libutils.a 

ptp_orte_testlib.o: ptp_orte_testlib.c
	$(CC) -c ptp_orte_testlib.c

clean:
	rm -f ptp_orte_proxy ptp_orte_testlib.o
