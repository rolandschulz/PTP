! This is a sample program

program Hello
  integer :: i = 3 !!! a variable !!!

  call buS(nF(i) + 1)

  print *, "The integer is ", i

contains ! a comment

subroutine buS(x) ! start
  integer, intent(in) :: x

  print *, "In the subroutine, the integer is ", x
end subroutine buS   ! end

end program

integer function nF(n) result(y)
  print *, "You sent", n, "to the function"
  y = n
end function
