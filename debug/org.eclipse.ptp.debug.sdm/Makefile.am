SUBDIRS = bitvector libmigdb libaif

bin_PROGRAMS =			sdm test_proxy test_proxy_rev

sdm_SOURCES =			src/client/startup.c\
						src/client/client.c \
						src/client/client_cmds.c \
						src/client/client_svr.c \
						src/server/server.c \
						src/server/server_cmds.c \
						src/server/backend.c \
						src/server/gdbmi_backend.c
					
sdm_CFLAGS =				-I./include -I./bitvector -I./libmigdb/src -I./libaif $(AM_CFLAGS)
sdm_LDADD =				libproxysvr.a libdbgutils.a libutils.a bitvector/.libs/libbitvector.a libmigdb/src/libmigdb.a libaif/libaif.a

test_proxy_SOURCES =		src/test/test_proxy.c \
						src/test/test_proxy_clnt.c
test_proxy_CFLAGS =		-I./include -I./bitvector -I./libaif $(AM_CFLAGS) -g
test_proxy_LDFLAGS = 		-g
test_proxy_LDADD =		libdbgclnt.a libproxyclnt.a libdbgutils.a libutils.a bitvector/.libs/libbitvector.a libaif/libaif.a

test_proxy_rev_SOURCES =	src/test/test_proxy_rev.c \
						src/test/test_proxy_clnt.c
test_proxy_rev_CFLAGS =	-I./include -I./bitvector -I./libaif $(AM_CFLAGS) -g
test_proxy_rev_LDFLAGS = 	-g
test_proxy_rev_LDADD =	libdbgclnt.a libproxyclnt.a libdbgutils.a libutils.a bitvector/.libs/libbitvector.a libaif/libaif.a

include_HEADERS =		include/breakpoint.h include/dbg.h include/location.h include/bitset.h include/stackframe.h

lib_LTLIBRARIES =
if PTPDBGJNI
	lib_LTLIBRARIES += libptp_dbg_jni.la
	libptp_dbg_jni_la_SOURCES = jni/dbg_jni.c
	libptp_dbg_jni_la_CFLAGS = $(AM_CFLAGS)
endif

lib_LIBRARIES =			libdbgclnt.a libproxyclnt.a libproxysvr.a libdbgutils.a libutils.a

libdbgclnt_a_SOURCES =	src/client/client_interface.c 
libdbgclnt_a_CFLAGS =		-I./include -I./bitvector -I./libaif $(AM_CFLAGS) -g

libproxyclnt_a_SOURCES =	src/proxy/proxy.c \
						src/proxy/proxy_clnt.c \
						src/proxy/proxy_tcp.c \
						src/proxy/proxy_tcp_clnt.c \
						src/proxy/proxy_event.c
libproxyclnt_a_CFLAGS =	-I./include $(AM_CFLAGS) -g

libproxysvr_a_SOURCES =	src/proxy/proxy.c \
						src/proxy/proxy_svr.c \
						src/proxy/proxy_tcp.c \
						src/proxy/proxy_tcp_svr.c \
						src/proxy/proxy_event.c
libproxysvr_a_CFLAGS =	-I./include -I./bitvector -I./libaif $(AM_CFLAGS) -g

libdbgutils_a_SOURCES =	src/utils/error.c \
						src/utils/breakpoint.c \
						src/utils/stackframe.c \
						src/utils/event.c
						
libdbgutils_a_CFLAGS =	-I./include -I./bitvector -I./libaif $(AM_CFLAGS) -g

libutils_a_SOURCES =		src/utils/bitset.c \
						src/utils/args.c \
						src/utils/list.c \
						src/utils/hash.c \
						src/utils/handler.c \
						src/utils/compat.c
						
libutils_a_CFLAGS =		-I./include -I./bitvector $(AM_CFLAGS) -g 