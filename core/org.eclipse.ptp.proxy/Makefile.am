UTILS =					../org.eclipse.ptp.utils

lib_LIBRARIES =			libproxyclnt.a libproxysvr.a

libproxyclnt_a_SOURCES =	src/proxy.c \
						src/proxy_clnt.c \
						src/proxy_tcp.c \
						src/proxy_tcp_clnt.c \
						src/proxy_event.c
libproxyclnt_a_CFLAGS =	-I./include -I$(UTILS)/include $(AM_CFLAGS) -g

libproxysvr_a_SOURCES =	src/proxy.c \
						src/proxy_svr.c \
						src/proxy_tcp.c \
						src/proxy_tcp_svr.c \
						src/proxy_event.c
libproxysvr_a_CFLAGS =	-I./include -I$(UTILS)/include $(AM_CFLAGS) -g
