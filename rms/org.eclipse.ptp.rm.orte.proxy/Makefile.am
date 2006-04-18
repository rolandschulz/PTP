PROXY =					../org.eclipse.ptp.proxy
UTILS =					../org.eclipse.ptp.utils
SUBDIRS =				$(PROXY) $(UTILS)

bin_PROGRAMS =			ptp_orte_proxy

ptp_orte_proxy_SOURCES =		src/ptp_orte_proxy.c
ptp_orte_proxy_CFLAGS =		-I$(PROXY)/include -I$(UTILS)/include $(AM_CFLAGS) -g
ptp_orte_proxy_LDADD =		$(PROXY)/libproxysvr.a $(UTILS)/libutils.a
