program common1
  implicit none

  common /block/ a, b, c !<<<<< 4, 11, 4, 16, aaa, bbb, ccc, pass
  integer :: a
  real :: b
  double precision :: c

  a = 5
  b = 4.6
  c = 2.345
  print *, a, b, c

  call helper
  print *, a, b, c
end program common1

subroutine helper
  implicit none
  common /block/ e, f
  integer :: e
  real :: f
  
  e = 50
  f = 40.6
end subroutine helper
