! Demonstration of Fortran 2008 submodules
! J. Overbey - 1 Dec 2009

! The module and submodules have the following hierarchy:
!
!               module
!                  |
!                  |
!             submodule1
!                  |
!                  |
!             submodule2
!                 /|\
!               /  |  \
!             /    |    \
!           /      |      \
! submodule3  submodule4  submodule5

module module
  implicit none
contains ! Empty contains section allowed in Fortran 2008
end module module

submodule (module) submodule1
  implicit none
end

submodule (module : submodule1) submodule2
end submodule

submodule (module : submodule2) submodule3
end submodule submodule3

submodule (module : submodule2) submodule4
endsubmodule

submodule (module : submodule2) submodule5
endsubmodule submodule3
