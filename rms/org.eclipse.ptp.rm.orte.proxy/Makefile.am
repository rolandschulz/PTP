PROXY =					../org.eclipse.ptp.proxy
UTILS =					../org.eclipse.ptp.utils
SUBDIRS =				$(PROXY) $(UTILS)

bin_PROGRAMS =			orte_server

orte_server_SOURCES =		src/orte_server.c
orte_server_CFLAGS =		-I$(PROXY)/include -I$(UTILS)/include $(AM_CFLAGS) -g
orte_server_LDADD =		$(PROXY)/libproxysvr.a $(UTILS)/libutils.a