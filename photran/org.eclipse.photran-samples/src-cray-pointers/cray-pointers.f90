! Examples using Cray pointers (non-standard extension)
! Jeff Overbey 8/27/08

implicit none

call example1
call example2
call example3
end

! Example 1: Pointer to scalar
subroutine example1()
	integer :: pointer           ! A Cray pointer is just an integer
	pointer (pointer, pointee)   ! We associate the pointer variable with another variable
	real    :: pointee           ! To dereference the pointer, we read this variable

	real :: value = 5.0

	pointer = loc(value)
	print *, pointee  ! = 5.0
end subroutine

! Example 2: Pointer to array, constant size
subroutine example2()
    integer :: pointer
    real    :: pointee
    pointer (pointer, pointee(5))

    real :: array(5) = (/ 1.1, 2.2, 3.3, 4.4, 5.5 /)

    pointer = loc(array)
    print *, pointee(3)  ! = 3.3
end subroutine

! Example 3: Pointer to array, arbitrary size
subroutine example3()
    integer :: pointer
    real    :: pointee
    pointer (pointer, pointee(*))

    real :: array(5) = (/ 1.1, 2.2, 3.3, 4.4, 5.5 /)

    pointer = loc(array)
    print *, pointee(-2)  ! = 3.3
    print *, pointee(3)   ! = 3.3
end subroutine
