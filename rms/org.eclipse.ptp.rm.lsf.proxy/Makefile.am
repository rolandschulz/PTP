PROXY =					../org.eclipse.ptp.proxy
UTILS =					../org.eclipse.ptp.utils
SUBDIRS =				$(PROXY) $(UTILS)

bin_PROGRAMS =			ptp_lsf_proxy

ptp_lsf_proxy_SOURCES =		src/ptp_lsf_proxy.c
ptp_lsf_proxy_CFLAGS =		-I$(PROXY)/include -I$(UTILS)/include $(AM_CFLAGS) -g
ptp_lsf_proxy_LDADD =		$(PROXY)/libproxysvr.a $(UTILS)/libutils.a
