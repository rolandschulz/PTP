program Hello
  implicit none
  integer :: i

  i = 3
  call Sub(i + 1)

  print *, "The integer is ", i

contains

subroutine Sub(i)
  integer :: i
  integer :: x

  x = i
  print *, x

end subroutine Sub


subroutine SomeSub(y,z)
  real :: y
  real :: z
  real :: x

  x = y + z
  y = 22
  z = y + z
  print *, x

end subroutine SomeSub

end program
