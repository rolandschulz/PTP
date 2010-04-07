CC = g++

CFLAGS =

OTHER_FLAGS = -ldl -lpthread -lsci

all: all_32 all_64

all_32: dsh_fe dsh_be downfilter.so upfilter.so upfiltera.so upfilterb.so
all_64: dsh_fe64 dsh_be64 downfilter64.so upfilter64.so upfiltera64.so upfilterb64.so

dsh_fe: dsh_fe.c 
	$(CC) -g -m32 $(CFLAGS) -o $@ $^ $(OTHER_FLAGS)

dsh_be: dsh_be.c 
	$(CC) -g -m32 $(CFLAGS) -o $@ $^ $(OTHER_FLAGS)

downfilter.so: downfilter.c
	$(CC) -g -m32 $(CFLAGS) -fpic -shared -o $@ $^ $(OTHER_FLAGS)

upfilter.so: upfilter.c
	$(CC) -g -m32 $(CFLAGS) -fpic -shared -o $@ $^ $(OTHER_FLAGS)

upfiltera.so: upfiltera.c
	$(CC) -g -m32 $(CFLAGS) -fpic -shared -o $@ $^ $(OTHER_FLAGS)

upfilterb.so: upfilterb.c
	$(CC) -g -m32 $(CFLAGS) -fpic -shared -o $@ $^ $(OTHER_FLAGS)
	
dsh_fe64: dsh_fe.c 
	$(CC) -m64 -D__64BIT__ -g $(CFLAGS) -o $@ $^ $(OTHER_FLAGS)

dsh_be64: dsh_be.c 
	$(CC) -m64 -D__64BIT__ -g $(CFLAGS) -o $@ $^ $(OTHER_FLAGS)

downfilter64.so: downfilter.c
	$(CC) -m64 -D__64BIT__ -g $(CFLAGS) -fpic -shared -o $@ $^ $(OTHER_FLAGS)

upfilter64.so: upfilter.c
	$(CC) -m64 -D__64BIT__ -g $(CFLAGS) -fpic -shared -o $@ $^ $(OTHER_FLAGS)

upfiltera64.so: upfiltera.c
	$(CC) -m64 -D__64BIT__ -g $(CFLAGS) -fpic -shared -o $@ $^ $(OTHER_FLAGS)

upfilterb64.so: upfilterb.c
	$(CC) -m64 -D__64BIT__ -g $(CFLAGS) -fpic -shared -o $@ $^ $(OTHER_FLAGS)

clean: clean_32 clean_64

clean_32:
	@rm -f *.o dsh_fe dsh_be downfilter.so upfilter.so upfiltera.so upfilterb.so

clean_64:
	@rm -f *.o dsh_fe64 dsh_be64 downfilter64.so upfilter64.so upfiltera64.so upfilterb64.so
