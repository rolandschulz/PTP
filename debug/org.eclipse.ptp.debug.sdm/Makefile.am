PROXY =					../org.eclipse.ptp.proxy
UTILS =					../org.eclipse.ptp.utils
SUBDIRS =				libaif $(PROXY) $(UTILS)

bin_PROGRAMS =			sdm
noinst_PROGRAMS =		test_proxy test_proxy_rev test_mi

sdm_SOURCES =			src/client/startup.c\
						src/client/client.c \
						src/client/client_cmds.c \
						src/client/client_svr.c \
						src/server/server.c \
						src/server/server_cmds.c \
						src/server/backend.c \
						src/server/gdbmi_backend.c
					
sdm_CFLAGS =				-I./include -I./libaif -I./src/mi -I$(PROXY)/include -I$(UTILS)/include $(AM_CFLAGS)
sdm_LDADD =				$(PROXY)/libproxysvr.a libdbgutils.a $(UTILS)/libutils.a libmi.a libaif/libaif.a

test_proxy_SOURCES =		src/test/test_proxy.c \
						src/test/test_proxy_clnt.c
test_proxy_CFLAGS =		-I./include -I./libaif -I$(PROXY)/include -I$(UTILS)/include $(AM_CFLAGS) -g
test_proxy_LDFLAGS = 		-g
test_proxy_LDADD =		libdbgclnt.a $(PROXY)/libproxyclnt.a libdbgutils.a $(UTILS)/libutils.a libaif/libaif.a

test_proxy_rev_SOURCES =	src/test/test_proxy_rev.c \
						src/test/test_proxy_clnt.c
test_proxy_rev_CFLAGS =	-I./include -I./libaif -I$(PROXY)/include -I$(UTILS)/include $(AM_CFLAGS) -g
test_proxy_rev_LDFLAGS = 	-g
test_proxy_rev_LDADD =	libdbgclnt.a $(PROXY)/libproxyclnt.a libdbgutils.a $(UTILS)/libutils.a libaif/libaif.a

test_mi_SOURCES =		src/mi/test_mi.c
test_mi_CFLAGS =			-I./include -I./src/mi -I$(UTILS)/include $(AM_CFLAGS) -g
test_mi_LDFLAGS = 		-g
test_mi_LDADD =			libmi.a $(UTILS)/libutils.a

noinst_HEADERS =		include/breakpoint.h include/dbg.h include/location.h include/stackframe.h include/memoryinfo.h include/signalinfo.h

noinst_LIBRARIES =		libdbgclnt.a libdbgutils.a libmi.a

libdbgclnt_a_SOURCES =	src/client/client_interface.c 
libdbgclnt_a_CFLAGS =		-I./include -I./libaif -I$(PROXY)/include -I$(UTILS)/include $(AM_CFLAGS) -g

libdbgutils_a_SOURCES =	src/utils/error.c \
						src/utils/breakpoint.c \
						src/utils/stackframe.c \
						src/utils/event.c \
						src/utils/itimer.c \
						src/utils/memoryinfo.c \
						src/utils/signalinfo.c
						
libdbgutils_a_CFLAGS =	-I./include -I./libaif -I$(PROXY)/include -I$(UTILS)/include $(AM_CFLAGS) -g

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
						src/mi/MISession.c \
						src/mi/MIThreadCommand.c \
						src/mi/MIMemory.c \
						src/mi/MIMemoryCommand.c \
						src/mi/MISigHandle.c \
						src/mi/CLISignalCommand.c
						
libmi_a_CFLAGS =			-I./include -I$(UTILS)/include $(AM_CFLAGS) -g 
