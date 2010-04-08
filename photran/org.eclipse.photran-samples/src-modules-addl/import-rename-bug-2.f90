! This demonstrates an inconsistency in the handling of modules with renaming.
! J. Overbey 8 Apr 2010

module m1; implicit none
  integer :: m
end module

module m2; use m1; implicit none
  integer :: n
end module

program test
  ! gfortran 4.4.2 allows this.
  ! IBM XL Fortran 12.1 fails, claiming x is in scope
  ! at the second rename clause.
  ! According to the Fortran 95
  ! standard (ISO/IEC 1539-1), Section 11.3.2:
  ! "Two or more accessible entities, other than
  ! generic interfaces, may have the same name only if
  ! the name is not used to refer to an entity in the
  ! scoping unit."
  ! The question is what "refer to" means.  If you
  ! interpret this to mean, "The name has no references,"
  ! then gfortran is correct.  If you interpret it to mean,
  ! "There are no other declarations/entities with that
  ! name," then XLF is correct.
  use m2, x => n, x => m
  implicit none
  print *, "Hi"
end program
