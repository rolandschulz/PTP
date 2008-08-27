!!
!! Sample Jacobi iteration computing heat transfer across a 2-D surface
!! J. Overbey 8/27/08
!!
!! Use jacobi-viz.sh in the main project directory to visualize the resulting
!! table using gnuplot
!!
!! This version looks the nicest and uses the eoshift intrinsic
!!
program jacobi_example
    implicit none

    integer, parameter :: SIZE = 200
    integer, parameter :: INTERIOR_SIZE = SIZE - 2
    real, parameter    :: BOUNDARY_VALUE = 5.0
    real, parameter    :: EPSILON = 0.001

    call main()

contains

subroutine main()
    integer :: i
    real :: surface(INTERIOR_SIZE, INTERIOR_SIZE)

    surface = 0.0
    surface = iterate(surface)

    print *, (BOUNDARY_VALUE, i=1,SIZE)
    do i = 1, INTERIOR_SIZE; print *, BOUNDARY_VALUE, surface(i, :), BOUNDARY_VALUE; end do
    print *, (BOUNDARY_VALUE, i=1,SIZE)
end subroutine

function iterate(initial) result(result)
    intent(in) :: initial
    real, dimension(INTERIOR_SIZE, INTERIOR_SIZE) :: initial, previous, up, down, left, right, result

    result = initial
    do
        previous = result
        up     = eoshift(previous, +1, boundary_value, 1)
        down   = eoshift(previous, -1, boundary_value, 1)
        left   = eoshift(previous, +1, boundary_value, 2)
        right  = eoshift(previous, -1, boundary_value, 2)
        result = (up + down + left + right) / 4.0
        if (maxval(abs(result - previous)) .lt. EPSILON) exit
    end do
end function

end program
