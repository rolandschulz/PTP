noinst_HEADERS =	metadata_header.h \
					x10_aif.h \
					x10_da.h \
					x10_dbg_types.h \
					x10_debug_line_info.h \
					x10_metadebug_info_map.h \
					x10_var.h \
					x10_variable.h \
					x10_variable_util.h

noinst_LIBRARIES =	libx10.a


libx10_a_SOURCES =	x10_gdbmi_backend.c \
					x10_aif.c \
					x10_da.c \
					x10_var.c \
					x10_variable.c \
					x10_metadebug_info_map.c \
					x10_variable_util.c
						
libx10_a_CFLAGS =	-I../include -I../libaif -I../libmi $(AM_CFLAGS) -g
