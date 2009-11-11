module mod
  implicit none
  integer, dimension(:), allocatable :: array  !3,27,5
end module

program encap1
  use mod
  implicit none

  allocate (array(5))
  array = (/ 10, 20, 30, 40, 50 /)
  print *, array
  deallocate (array)
end program encap1
