SUBDIRS =					libaif libmi

bin_PROGRAMS =				sdm
noinst_PROGRAMS =			sdm_test

sdm_SOURCES =				src/master/startup.c\
							src/master/master.c \
							src/master/master_cmds.c \
							src/server/server.c \
							src/server/server_cmds.c \
							src/server/backend.c \
							src/server/gdbmi_backend.c \
							src/server/test_backend.c
sdm_CFLAGS =				-I./include -I./libaif -I./libmi  $(AM_CFLAGS)
sdm_LDADD =					libsdm.a libdbgutils.a libmi/libmi.a libaif/libaif.a libmi/libmi.a @SVR_LIBS@
							
libsdm_a_SOURCES =			src/impl/sdm.c \
							src/impl/sdm_aggregate_hash.c \
							src/impl/sdm_hello_proto.c \
							src/impl/sdm_message_tcpip.c \
							src/impl/sdm_route_tree.c \ \
							src/impl/sdm_routing_table_file.c \
							src/impl/sdm_set_bitset.c
					
libsdm_a_CFLAGS =			-I./include $(AM_CFLAGS)

sdm_test_SOURCES =			src/test/sdm_test.c
sdm_test_CFLAGS =			-I./include -I./libaif -I./libmi $(AM_CFLAGS) -g
sdm_test_LDFLAGS = 			-g
sdm_test_LDADD =			libdbgclnt.a libsdm.a libdbgutils.a libaif/libaif.a libmi/libmi.a @CLNT_LIBS@

noinst_HEADERS =			include/backend.h \
							include/breakpoint.h \
							include/dbg_master.h \
							include/dbg_error.h \
							include/dbg_event.h \
							include/dbg_proxy.h \
							include/dbg.h \
							include/helloproto.h \
							include/itimer.h \
							include/location.h \
							include/memoryinfo.h \
							include/routing_table.h \
							include/sdm.h \
							include/session.h \
							include/stackframe.h \
							include/signal_info.h

noinst_LIBRARIES =			libsdm.a libdbgclnt.a libdbgutils.a

libdbgclnt_a_SOURCES =		src/client/client_interface.c 
libdbgclnt_a_CFLAGS =		-I./include -I./libaif -I./libmi $(AM_CFLAGS) -g

libdbgutils_a_SOURCES =		src/utils/error.c \
							src/utils/breakpoint.c \
							src/utils/bpmap.c \
							src/utils/stackframe.c \
							src/utils/signal_info.c \
							src/utils/event.c \
							src/utils/itimer.c \
							src/utils/memoryinfo.c \
							src/utils/debug.c \
							src/utils/gdb_aif.c \
							src/utils/gdb_mi.c
						
libdbgutils_a_CFLAGS =		-I./include -I./libaif -I./libmi $(AM_CFLAGS) -g
