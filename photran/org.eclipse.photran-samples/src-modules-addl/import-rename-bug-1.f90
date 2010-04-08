! This demonstrates an inconsistency in the handling of modules with renaming.
! J. Overbey 8 Apr 2010

module m1
  implicit none
  integer :: m
  integer :: n
end module

module m2
  implicit none
  integer :: o
  integer :: p
end module

module helper; implicit none; contains
  subroutine init
    use m1
    use m2
    m = 1
    n = 2
    o = 3
    p = 4
  end subroutine
end module

program test
  use helper
  use m1, n => m ! m is inaccessible
                 ! Does the local n shadow the module entity n?
  use m2, oo => p
  implicit none
  integer :: p
  p = 5
  call init
  
  ! There is ambiguity in the Fortran 95 standard as to how the following
  ! statement should be handled.  According to Section 11.3.2:
  ! "Two or more accessible entities, other than
  ! generic interfaces, may have the same name only if
  ! the name is not used to refer to an entity in the
  ! scoping unit. ... the local name of any entity
  ! given accessibility by a USE statement shall differ
  ! from the local names of all other entities accessible
  ! to the scoping unit through USE statements and
  ! otherwise."
  ! GNU Fortran 4.4.2:   Compilation fails: n is an ambiguous reference
  ! IBM XL Fortran 12.1: Outputs 2 3 5
  ! Intel Fortran 10.1:  Outputs 2 3 5
  ! PGI Fortran 10.0:    Outputs 1 3 5
  print *, n, o, p
  
end program