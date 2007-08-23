program main
  external C_INT
  integer :: i
  call C_INT(i)
  print *, "C_INT = ", i
end program main
