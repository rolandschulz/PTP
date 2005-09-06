bin_PROGRAMS = sdm

sdm_SOURCES = src/client/client.c src/client/client_interface.c src/client/startup.c src/server/server.c src/utils/procset.c
sdm_CFLAGS = -I./include -I./bitvector $(AM_CFLAGS)

include_HEADERS = include/breakpoint.h include/dbg.h include/location.h include/procset.h include/stackframe.h


lib_LTLIBRARIES = libptp_dbg_jni.la
libptp_dbg_jni_la_SOURCES = jni/dbg_jni.c
libptp_dbg_jni_la_LDFLAGS = -shrext .jnilib -single_module -framework JavaVM
libptp_dbg_jni_la_CFLAGS = -I/System/Library/Frameworks/JavaVM.framework/Headers $(AM_CFLAGS)
