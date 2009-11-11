module mod
  implicit none
  integer, pointer :: ptr  !3,23,3
end module

program encap1
  use mod
  implicit none

  integer, target :: int
  int = 3
  ptr => int
  int = 4
  print *, ptr
end program encap1
