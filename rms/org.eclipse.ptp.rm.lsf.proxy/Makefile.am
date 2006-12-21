LSF =					/LSF
PROXY =					../org.eclipse.ptp.proxy
UTILS =					../org.eclipse.ptp.utils
SUBDIRS =				$(PROXY) $(UTILS)

bin_PROGRAMS =			ptp_lsf_proxy

ptp_lsf_proxy_HEADERS =	include/ptp_lsf_proxy.h
ptp_lsf_proxydir = src
ptp_lsf_proxy_SOURCES =	src/ptp_lsf_proxy.c
ptp_lsf_proxy_CFLAGS =	-I./include -I$(PROXY)/include -I$(UTILS)/include -I$(LSF)/share/include $(AM_CFLAGS) -g
ptp_lsf_proxy_LDADD =	$(PROXY)/libproxysvr.a $(UTILS)/libutils.a
