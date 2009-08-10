module mod
  implicit none
  integer :: variable  !3,14,8
end module

program encap1
  use mod
  implicit none
  variable = 3
  !                            9 + 1
  variable = variable * variable + (variable - 2)
  print *, variable, " should be equal to ", 10
end program encap1
