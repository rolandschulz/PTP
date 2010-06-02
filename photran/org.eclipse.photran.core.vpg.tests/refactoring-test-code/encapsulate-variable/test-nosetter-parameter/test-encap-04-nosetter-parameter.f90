module mod
  implicit none
  integer, parameter :: CONSTANT = 10 !<<<<< 3, 25, 8, fail-initial
end module

program encap1
  use mod
  implicit none

  print *, CONSTANT
end program encap1
