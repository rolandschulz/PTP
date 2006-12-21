#
# Makes a replacement LSF lsb_* batch library for testing
#

LSF = /LSF

PROXY_OBJ = ../../org.eclipse.ptp.lsf.proxy/ptp_lsf_proxy-ptp_lsf_proxy.o

UTIL_PATH = ../../org.eclipse.ptp.utils

PROXY_SVR_PATH = ../../org.eclipse.ptp.proxy

all: ptp_lsf_proxy

ptp_lsf_proxy: ptp_lsf_testlib.o
	$(CC) -o ptp_lsf_proxy $(PROXY_OBJ) ptp_lsf_testlib.o $(PROXY_SVR_PATH)/libproxysvr.a $(UTIL_PATH)/libutils.a 

ptp_lsf_testlib.o: ptp_lsf_testlib.c
	$(CC) -c -I$(LSF)/share/include ptp_lsf_testlib.c

clean:
	rm -f ptp_lsf_proxy ptp_lsf_testlib.o
