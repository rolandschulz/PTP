! This demonstrates an inconsistency in the handling of modules with renaming.
! J. Overbey 8 Apr 2010

module m1; implicit none
  integer :: m
end module

module m2; use m1; implicit none
  integer :: n
end module

program test
  ! There is ambiguity in the Fortran 95 standard as to how the following
  ! statement should be handled.  According to Section 11.3.2:
  ! "Two or more accessible entities, other than
  ! generic interfaces, may have the same name only if
  ! the name is not used to refer to an entity in the
  ! scoping unit."
  ! The question is what "refer to" means.  If you
  ! interpret this to mean, "The name has no references,"
  ! then compilation should succeed.  If you interpret it to mean,
  ! "There are no other declarations/entities with that
  ! name," then compilation should fail.
  ! GNU Fortran 4.4.2:   Compilation succeeds.
  ! IBM XL Fortran 12.1: Compilation fails: "Identifier "x" to be used as the
  !                          the local name in a rename, has already been used
  !                          to access another entity by use-association. This
  !                          rename will not be done."
  ! Intel Fortran 10.1:  Compilation fails: "There is more than one use-name
  !                          for a local-name. [X]"
  ! PGI Fortran 10.0:    Compilation succeeds.
  use m2, x => n, x => m
  implicit none
  print *, "Hi"
end program
