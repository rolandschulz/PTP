SUBDIRS = libaif

bin_PROGRAMS =			sdm test_proxy test_proxy_rev test_mi

sdm_SOURCES =			src/client/startup.c\
						src/client/client.c \
						src/client/client_cmds.c \
						src/client/client_svr.c \
						src/server/server.c \
						src/server/server_cmds.c \
						src/server/backend.c \
						src/server/gdbmi_backend.c
					
sdm_CFLAGS =				-I./include -I./libaif -I./src/mi $(AM_CFLAGS)
sdm_LDADD =				libproxysvr.a libdbgutils.a libutils.a libmi.a libaif/libaif.a

test_proxy_SOURCES =		src/test/test_proxy.c \
						src/test/test_proxy_clnt.c
test_proxy_CFLAGS =		-I./include -I./libaif $(AM_CFLAGS) -g
test_proxy_LDFLAGS = 		-g
test_proxy_LDADD =		libdbgclnt.a libproxyclnt.a libdbgutils.a libutils.a libaif/libaif.a

test_proxy_rev_SOURCES =	src/test/test_proxy_rev.c \
						src/test/test_proxy_clnt.c
test_proxy_rev_CFLAGS =	-I./include -I./libaif $(AM_CFLAGS) -g
test_proxy_rev_LDFLAGS = 	-g
test_proxy_rev_LDADD =	libdbgclnt.a libproxyclnt.a libdbgutils.a libutils.a libaif/libaif.a

test_mi_SOURCES =		src/mi/test_mi.c
test_mi_CFLAGS =			-I./include -I./src/mi $(AM_CFLAGS) -g
test_mi_LDFLAGS = 		-g
test_mi_LDADD =			libmi.a libutils.a

include_HEADERS =		include/breakpoint.h include/dbg.h include/location.h include/bitset.h include/stackframe.h

lib_LTLIBRARIES =
if PTPDBGJNI
	lib_LTLIBRARIES += libptp_dbg_jni.la
	libptp_dbg_jni_la_SOURCES = jni/dbg_jni.c
	libptp_dbg_jni_la_CFLAGS = $(AM_CFLAGS)
endif

lib_LIBRARIES =			libdbgclnt.a libproxyclnt.a libproxysvr.a libdbgutils.a \
						libmi.a libutils.a

libdbgclnt_a_SOURCES =	src/client/client_interface.c 
libdbgclnt_a_CFLAGS =		-I./include  -I./libaif $(AM_CFLAGS) -g

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
libproxysvr_a_CFLAGS =	-I./include -I./libaif $(AM_CFLAGS) -g

libdbgutils_a_SOURCES =	src/utils/error.c \
						src/utils/breakpoint.c \
						src/utils/stackframe.c \
						src/utils/event.c
						
libdbgutils_a_CFLAGS =	-I./include -I./libaif $(AM_CFLAGS) -g

libmi_a_SOURCES =		src/mi/MIParser.c \
						src/mi/MIOOBRecord.c \
						src/mi/MIOutput.c \
						src/mi/MIResult.c \
						src/mi/MIResultRecord.c \
						src/mi/MIValue.c \
						src/mi/MIString.c \
						src/mi/MIBreakpoint.c \
						src/mi/MIFrame.c \
						src/mi/MIArg.c \
						src/mi/MIVar.c \
						src/mi/MIEvent.c \
						src/mi/MICommand.c \
						src/mi/MIGDBCommand.c \
						src/mi/MIExecCommand.c \
						src/mi/MIBreakCommand.c \
						src/mi/MIStackCommand.c \
						src/mi/MIVarCommand.c \
						src/mi/MIError.c \
						src/mi/MISession.c
						
libmi_a_CFLAGS =			-I./include $(AM_CFLAGS) -g 

libutils_a_SOURCES =		src/utils/bitset.c \
						src/utils/args.c \
						src/utils/list.c \
						src/utils/hash.c \
						src/utils/handler.c \
						src/utils/compat.c
						
libutils_a_CFLAGS =		-I./include $(AM_CFLAGS) -g 