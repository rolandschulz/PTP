bin_PROGRAMS = scia
scia_SOURCES = scia.cpp
if SYSTEM_AIX
scia_CPPFLAGS = -q64 -I../common -I../include
scia_LDFLAGS = -q64 -L../common -lcmn64 -lrt -lpthread -ldl
endif
if SYSTEM_LINUX
scia_CPPFLAGS = -I../common -I../include
scia_LDFLAGS = -L../common -lcmn -lrt -lpthread -ldl
endif
if SYSTEM_DARWIN
scia_CPPFLAGS = -I../common -I../include
scia_LDFLAGS = -L../common -lcmn -lpthread -ldl
endif