module mod
  implicit none
  integer :: variable !<<<<< 3, 14, 8, fail-final
contains
  integer function getVariable()
    getVariable = 99999
  end function
end module

program encap1
  use mod
  implicit none
  print *, variable
  variable = 3
  print *, variable
end program encap1
