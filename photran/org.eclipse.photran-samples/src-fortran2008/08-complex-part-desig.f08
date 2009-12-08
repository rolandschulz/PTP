! Exercises Fortran 2008 complex part designators (R615)
! J. Overbey - 7 Dec 2009

implicit none

complex :: cmplx = (3.0, 4.0)
print *, cmplx
print *, cmplx%re
print *, cmplx%im
print *, cmplx%RE
print *, cmplx%IM

end program
