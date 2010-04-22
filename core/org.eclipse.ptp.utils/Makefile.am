noinst_LIBRARIES =		libutils.a
noinst_PROGRAMS =		bitset_test \
						args_test \
						rangeset_test

libutils_a_SOURCES =	src/bitset.c \
						src/args.c \
						src/list.c \
						src/hash.c \
						src/handler.c \
						src/compat.c \
						src/rangeset.c \
						src/serdes.c \
						src/varstr.c
						
libutils_a_CFLAGS =		-I./include $(AM_CFLAGS) -g 

bitset_test_SOURCES =	src/bitset_test.c
bitset_test_CFLAGS =	-I./include $(AM_CFLAGS) -g
bitset_test_LDFLAGS =	-g
bitset_test_LDADD =		libutils.a

args_test_SOURCES =		src/args_test.c
args_test_CFLAGS =		-I./include $(AM_CFLAGS) -g
args_test_LDFLAGS =		-g
args_test_LDADD =		libutils.a

rangeset_test_SOURCES =	src/rangeset_test.c
rangeset_test_CFLAGS =	-I./include $(AM_CFLAGS) -g
rangeset_test_LDFLAGS =	-g
rangeset_test_LDADD =	libutils.a
