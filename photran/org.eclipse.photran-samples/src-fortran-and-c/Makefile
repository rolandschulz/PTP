# Requires g95 under Windows (since calling a C function; proprietary)

clean:
	rm -f a.exe *.o

all:
	gcc -g -c c.c
	g95 -g -c fortran.f90
	g95 -o a.exe c.o fortran.o
	rm *.o
