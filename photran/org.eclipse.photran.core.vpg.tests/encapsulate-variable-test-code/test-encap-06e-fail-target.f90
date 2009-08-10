module mod
  implicit none
  integer, target :: int
end module

program encap1
  use mod
  implicit none

  integer, pointer :: ptr  !3,22,3
  int = 3
  ptr => int
  int = 4
  print *, ptr
end program encap1
