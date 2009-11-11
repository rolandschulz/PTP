module mod
  implicit none
  integer :: variable  !3,14,8
end module

program encap1
  use mod
  implicit none
  print *, variable
  variable = 3
  print *, variable
end program encap1
