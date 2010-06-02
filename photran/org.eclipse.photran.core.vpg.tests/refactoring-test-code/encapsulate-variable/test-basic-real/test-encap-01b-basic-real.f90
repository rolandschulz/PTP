module mod
  implicit none
  real :: variable !<<<<< 3, 11, 8, pass
end module

program encap1
  use mod
  implicit none
  print *, variable
  variable = 3.0
  print *, variable
end program encap1
