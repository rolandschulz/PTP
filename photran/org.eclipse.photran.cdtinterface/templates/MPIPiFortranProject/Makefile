# ============================================================================
# Name        : Makefile
# Author      : {{author}}
# Version     :
# Copyright   : {{copyright}}
# Description : Makefile for Hello MPI World in Fortran
# ============================================================================

.PHONY: all clean

all: {{sourceDir}}/{{baseName}}.f90
	mpif90 -O2 -g -o {{binDir}}/{{baseName}}{{exe}} \
		{{sourceDir}}/{{baseName}}.f90

clean:
	rm -f {{binDir}}/{{baseName}}{{exe}} *.mod
