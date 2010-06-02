module mod
  implicit none
  integer, dimension(:), allocatable :: array !<<<<< 3, 27, 5, fail-initial
contains
  subroutine alloc
    allocate (array(5))
  end subroutine
  subroutine dealloc
    deallocate (array)
  end subroutine
end module

program encap1
  use mod
  implicit none

  call alloc
  array = (/ 10, 20, 30, 40, 50 /)
  print *, array
  call dealloc
end program encap1
