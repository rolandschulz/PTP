module mod
  implicit none
  integer, dimension(3) :: variable !<<<<< 3, 28, 8, fail-initial
end module

program encap1
  use mod
  implicit none
  variable = (/ 1, 2, 3 /)
  print *, variable
end program encap1
