noinst_PROGRAMS =			testaif
noinst_LIBRARIES =			libaif.a

libaif_a_SOURCES =			aif.c \
							calc.c \
							fds.c \
							array.c \
							conv.c \
							cmp.c \
							print.c \
							error.c \
							io.c \
							struct.c \
							util.c \
							aif.x
					
libaif_a_CFLAGS =			$(AM_CFLAGS) -Wall

testaif_SOURCES =			testaif.c
testaif_CFLAGS =			-I. $(AM_CFLAGS) -g
testaif_LDFLAGS = 			-g
testaif_LDADD =				libaif.a