noinst_LIBRARIES =		libutils.a
noinst_PROGRAMS =		bitset_test

libutils_a_SOURCES =	src/bitset.c \
						src/args.c \
						src/list.c \
						src/hash.c \
						src/handler.c \
						src/compat.c
						
libutils_a_CFLAGS =		-I./include $(AM_CFLAGS) -g 

bitset_test_SOURCES =	src/bitset_test.c
bitset_test_CFLAGS =	-I./include $(AM_CFLAGS) -g
bitset_test_LDFLAGS =	-g
bitset_test_LDADD =		libutils.a
