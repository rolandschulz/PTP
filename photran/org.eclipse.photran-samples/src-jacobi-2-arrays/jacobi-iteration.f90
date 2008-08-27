!!
!! Sample Jacobi iteration computing heat transfer across a 2-D surface
!! J. Overbey 8/27/08
!!
!! Use jacobi-viz.sh in the main project directory to visualize the resulting
!! table using gnuplot
!!
!! This version uses array sections to compute each iteration and the delta
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
    real :: surface(SIZE, SIZE)
    integer :: i

    call jacobi(surface)

    do i = 1, SIZE; print *, surface(i, :); end do
end subroutine

subroutine jacobi(surface)
    real, intent(out) :: surface(SIZE, SIZE)
    real, dimension(SIZE, SIZE) :: prev, next
    real :: delta

    call init_with_boundaries(prev)
    call init_with_boundaries(next)

    do
        delta = iterate(prev, next)
        if (delta < EPSILON) then
            surface = next
            return
        end if

        delta = iterate(next, prev)
        if (delta < EPSILON) then
            surface = prev
            return
        end if
    end do
end subroutine

subroutine init_with_boundaries(surface)
    real, dimension(SIZE, SIZE), intent(in out) :: surface

    surface          = 0.0
    surface(1, :)    = BOUNDARY_VALUE
    surface(SIZE, :) = BOUNDARY_VALUE
    surface(:, 1)    = BOUNDARY_VALUE
    surface(:, SIZE) = BOUNDARY_VALUE
end subroutine

function iterate(prev, next) result(epsilon)
    real, dimension(SIZE, SIZE), intent(in)  :: prev
    real, dimension(SIZE, SIZE), intent(in out) :: next
    real :: epsilon

    next(2:SIZE-1, 2:SIZE-1) = &
        (prev(1:SIZE-2, 2:SIZE-1) + &
         prev(3:SIZE,   2:SIZE-1) + &
         prev(2:SIZE-1, 1:SIZE-2) + &
         prev(2:SIZE-1, 3:SIZE  )) / 4.0
    epsilon = maxval(abs(next(2:SIZE-1,2:SIZE-1) - prev(2:SIZE-1,2:SIZE-1)))
end function

end program
