SUBDIRS = bitvector libmigdb libaif

bin_PROGRAMS = sdm test_proxy_clnt

sdm_SOURCES = src/client/client.c src/server/server.c src/client/startup.c src/proxy/proxy_svr.c \
					src/proxy/proxy_tcp_svr.c src/client/client_cmds.c src/server/server_dispatch.c \
					src/server/server_cmds.c src/server/backend.c src/server/gdbmi_backend.c
sdm_CFLAGS = -I./include -I./bitvector -I./libmigdb/src -I./libaif $(AM_CFLAGS)
sdm_LDADD = libdbgproxy.a libutils.a bitvector/.libs/libbitvector.a libmigdb/src/libmigdb.a libaif/libaif.a

test_proxy_clnt_SOURCES = src/test/test_proxy_clnt.c src/proxy/proxy.c src/proxy/proxy_clnt.c \
					src/proxy/proxy_tcp_clnt.c src/proxy/proxy_tcp.c
test_proxy_clnt_CFLAGS = -I./include -I./bitvector -I./libaif $(AM_CFLAGS) -g
test_proxy_clnt_LDFLAGS = -g
test_proxy_clnt_LDADD = libdbgproxy.a libutils.a bitvector/.libs/libbitvector.a libaif/libaif.a
test_proxy_svr_SOURCES = src/test/test_proxy_svr.c src/proxy/proxy.c src/proxy/proxy_svr.c \
					src/proxy/proxy_tcp_svr.c src/proxy/proxy_tcp.c src/client/client_cmds.c
test_proxy_svr_CFLAGS = -I./include -I./bitvector -I./libaif $(AM_CFLAGS) -g
test_proxy_svr_LDFLAGS = -g
test_proxy_svr_LDADD = libutils.a bitvector/.libs/libbitvector.a

include_HEADERS = include/breakpoint.h include/dbg.h include/location.h include/procset.h include/stackframe.h

lib_LTLIBRARIES = libptp_dbg_jni.la

libptp_dbg_jni_la_SOURCES = jni/dbg_jni.c
libptp_dbg_jni_la_LDFLAGS = -shrext .jnilib -single_module -framework JavaVM
libptp_dbg_jni_la_CFLAGS = -I/System/Library/Frameworks/JavaVM.framework/Headers $(AM_CFLAGS)

lib_LIBRARIES = libdbgproxy.a libutils.a

libdbgproxy_a_SOURCES = src/client/client_interface.c src/proxy/proxy.c src/proxy/proxy_tcp.c \
						src/proxy/proxy_tcp_clnt.c src/proxy/proxy_tcp_event.c
libdbgproxy_a_CFLAGS = -I./include -I./bitvector -I./libaif $(AM_CFLAGS) -g

libutils_a_SOURCES = src/utils/procset.c src/utils/args.c src/utils/list.c src/utils/error.c src/utils/breakpoint.c \
					src/utils/stackframe.c src/utils/event.c src/utils/hash.c
libutils_a_CFLAGS = -I./include -I./bitvector -I./libaif $(AM_CFLAGS) -g 