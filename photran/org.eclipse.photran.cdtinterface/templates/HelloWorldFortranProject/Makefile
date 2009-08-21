# ============================================================================
# Name        : Makefile
# Author      : {{author}}
# Version     :
# Copyright   : {{copyright}}
# Description : Makefile for Hello World in Fortran
# ============================================================================

.PHONY: all clean

# Change this line if you are using a different Fortran compiler
FORTRAN_COMPILER = gfortran

all: {{sourceDir}}/{{baseName}}.f90
	$(FORTRAN_COMPILER) -O2 -g \
		-o {{binDir}}/{{baseName}}{{exe}} \
		{{sourceDir}}/{{baseName}}.f90

clean:
	rm -f {{binDir}}/{{baseName}}{{exe}} *.mod
