# ============================================================================
# Name        : Makefile
# Author      : {{author}}
# Version     :
# Copyright   : {{copyright}}
# Description : Makefile for Hello MPI World in Fortran
# ============================================================================

# List of all source files in the project
SRC = {{baseName}}.f90

# If your MPI installation is not found, include -I/path/to/mpi/include here
INCLUDES = -I. -I/usr/local/include

# Set up the command to run the mpif90 compiler
MPIF90      = mpif90
MPIF90FLAGS = -O2 -g $(INCLUDES)

# Suffix Rules ===============================================================

.SUFFIXES: .f90

.f90.o:
	$(MPIF90) $(MPIF90FLAGS) -o $(addprefix {{binDir}}/, $(addsuffix .o, $(basename $<))) -c $(addprefix {{sourceDir}}/, $<)

.f90:

# Make Targets ===============================================================

.PHONY: all clean

OBJ = $(addprefix {{binDir}}/, $(addsuffix .o, $(addprefix {{sourceDir}}/, $(basename $(SRC)))))

{{binDir}}/{{baseName}}{{exe}}: $(OBJ)
	$(MPIF90) $(MPIF90FLAGS) -o $@ $(OBJ)

all: {{binDir}}/{{baseName}}{{exe}}

clean:
	rm -f {{baseName}}{{exe}} *.mod *.o
