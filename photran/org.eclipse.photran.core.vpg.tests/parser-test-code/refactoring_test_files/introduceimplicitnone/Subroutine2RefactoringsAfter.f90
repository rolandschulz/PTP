program Hello
  integer :: i = 3

  call Sub(i + 1)

  print *, "The integer is ", i

contains

subroutine Sub(i)
  implicit none
  integer :: i
  real :: x

  x = i
  print *, x

end subroutine Sub


subroutine SomeSub(y,z)
  implicit none
  real :: y
  real :: z
  real :: x

  x = y + z
  y = 22
  z = y + z
  print *, x

end subroutine SomeSub

end program
