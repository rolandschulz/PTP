FORTRAN=ifort
FLAGS=-g -I.

all:
	$(FORTRAN) $(FLAGS) -o rename1 rename1.f90
	$(FORTRAN) $(FLAGS) -o rename2 rename2.f90
	
	$(FORTRAN) $(FLAGS) -S rename3c.f90
	$(FORTRAN) $(FLAGS) -S rename3a.f90 rename3b.f90
	$(FORTRAN) $(FLAGS) -o rename3 rename3*.f90
	rm -f *.mod *.s
	
	$(FORTRAN) $(FLAGS) -o rename4 rename4.f90
	$(FORTRAN) $(FLAGS) -o rename5 rename5.f90
	$(FORTRAN) $(FLAGS) -o rename6 rename6.f90
	$(FORTRAN) $(FLAGS) -o rename7 rename7.f90
	$(FORTRAN) $(FLAGS) -o rename8 rename8.f90
	$(FORTRAN) $(FLAGS) -o rename9 rename9.f90 rename9a.f90

clean:
	rm -f rename? *.mod *.s
