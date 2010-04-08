! If A imports B and B imports C, then A imports entities from C
! (modulo renaming and ONLY lists).
! J. Overbey (8 Apr 2010)

module m1; implicit none
  integer :: m
end module

module m2; use m1; implicit none
  integer :: n
end module

program test
  use m2
  implicit none
  print *, m, n
end program