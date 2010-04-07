CC = g++

CPPFLAGS = -I../../include -L../../libsci

OTHER_FLAGS = -ldl -lpthread -lsci

all: all_32 all_64

all_32: dsh_fe edsh_fe gdsh_fe ddsh_fe dsh_be
all_64: dsh_fe64 edsh_fe64 gdsh_fe64 ddsh_fe64 dsh_be64

dsh_fe: dsh_fe.c 
	$(CC) -m32 -g $(CPPFLAGS) -o $@ $^ $(OTHER_FLAGS)

edsh_fe: edsh_fe.cpp 
	$(CC) -m32 -g $(CPPFLAGS) -o $@ $^ $(OTHER_FLAGS)

gdsh_fe: gdsh_fe.cpp 
	$(CC) -m32 -g $(CPPFLAGS) -o $@ $^ $(OTHER_FLAGS)

ddsh_fe: ddsh_fe.c
	$(CC) -m32 -g $(CPPFLAGS) -o $@ $^ $(OTHER_FLAGS)

dsh_be: dsh_be.c 
	$(CC) -m32 -g $(CPPFLAGS) -o $@ $^ $(OTHER_FLAGS)
	
dsh_fe64: dsh_fe.c 
	$(CC) -m64 -D__64BIT__ -g $(CPPFLAGS) -o $@ $^ $(OTHER_FLAGS)

edsh_fe64: edsh_fe.cpp 
	$(CC) -m64 -D__64BIT__ -g $(CPPFLAGS) -o $@ $^ $(OTHER_FLAGS)

gdsh_fe64: gdsh_fe.cpp 
	$(CC) -m64 -D__64BIT__ -g $(CPPFLAGS) -o $@ $^ $(OTHER_FLAGS)

ddsh_fe64: ddsh_fe.c
	$(CC) -m64 -D__64BIT__ -g $(CPPFLAGS) -o $@ $^ $(OTHER_FLAGS)

dsh_be64: dsh_be.c 
	$(CC) -m64 -D__64BIT__ -g $(CPPFLAGS) -o $@ $^ $(OTHER_FLAGS)

clean: clean_32 clean_64

clean_32:
	@rm -f *.o dsh_fe edsh_fe gdsh_fe ddsh_fe dsh_be

clean_64:
	@rm -f *.o dsh_fe64 edsh_fe64 gdsh_fe64 ddsh_fe64 dsh_be64
