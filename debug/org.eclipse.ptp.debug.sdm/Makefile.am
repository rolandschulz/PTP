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
sdm_CFLAGS =				-I./include -I./libaif -I./libmi $(AM_CFLAGS)
if X10_BACKEND
sdm_LDADD =					libsdm.a libx10.a libdbgutils.a libmi/libmi.a libaif/libaif.a libmi/libmi.a @SVR_LIBS@
else
sdm_LDADD =					libsdm.a libdbgutils.a libmi/libmi.a libaif/libaif.a libmi/libmi.a @SVR_LIBS@
endif
							
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
							include/disassembly.h \
							include/routing_table.h \
							include/sdm.h \
							include/session.h \
							include/stackframe.h \
							include/signal_info.h \
							include/x10/metadata_header.h \
							include/x10/x10_aif.h \
							include/x10/x10_da.h \
							include/x10/x10_dbg_types.h \
							include/x10/x10_debug_line_info.h \
							include/x10/x10_metadebug_info_map.h \
							include/x10/x10_var.h \
							include/x10/x10_variable.h \
							include/x10/x10_variable_util.h

noinst_LIBRARIES =			libsdm.a libdbgclnt.a libdbgutils.a
if X10_BACKEND
noinst_LIBRARIES += libx10.a							
endif

libdbgclnt_a_SOURCES =		src/client/client_interface.c 
libdbgclnt_a_CFLAGS =		-I./include -I./libaif -I./libmi $(AM_CFLAGS) -g

libdbgutils_a_SOURCES =		src/utils/error.c \
							src/utils/breakpoint.c \
							src/utils/bpmap.c \
							src/utils/threadmap.c \
							src/utils/stackframe.c \
							src/utils/signal_info.c \
							src/utils/event.c \
							src/utils/itimer.c \
							src/utils/memoryinfo.c \
							src/utils/disassembly.c \
							src/utils/debug.c \
							src/utils/gdb_aif.c \
							src/utils/gdb_mi.c
						
libdbgutils_a_CFLAGS =		-I./include -I./libaif -I./libmi $(AM_CFLAGS) -g

if X10_BACKEND
libx10_a_SOURCES =			src/x10/x10_gdbmi_backend.c \
							src/x10/x10_aif.c \
							src/x10/x10_da.c \
							src/x10/x10_var.c \
							src/x10/x10_variable.c \
							src/x10/x10_metadebug_info_map.c \
							src/x10/x10_variable_util.c
						
libx10_a_CFLAGS =			-I./include -I./libaif -I./libmi $(AM_CFLAGS) -g
endif