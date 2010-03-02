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

end program
