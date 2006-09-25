! This is a sample program

program Hello
  integer :: i = 3 !!! a variable !!!

  call Sub(Fn(i) + 1)

  print *, "The integer is ", i

contains ! a comment

subroutine Sub(x) ! start

  print *, "In the subroutine, the integer is ", x
end subroutine Sub   ! end

end program

integer function Fn(n) result(y)
  print *, "You sent", n, "to the function"
  y = n
end function
