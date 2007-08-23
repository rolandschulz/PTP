program Hello
  integer :: i = 3

  call Sub(i + 1)

  print *, "The integer is ", i
  
contains

subroutine Sub(x)
  integer, intent(in) :: x

  print *, "In the subroutine, the integer is ", x
end subroutine Sub

end program
