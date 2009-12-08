! Demonstrates the Fortran 2008 ALLSTOP statement
! Exercises R214 and R856
! J. Overbey - 8 Dec 2009
implicit none

stop=3
all=5
allstop=8
stop=all

stop "X"
stop 3
stop

all stop "X"
all stop 3
all stop
allstop "X"
allstop 3
allstop

end program
