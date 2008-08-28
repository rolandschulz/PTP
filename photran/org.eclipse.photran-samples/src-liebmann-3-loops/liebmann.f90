!!
!! Liebmann's method to compute heat transfer across a 2-D surface
!! J. Overbey 8/27/08
!!
!! Use liebmann-viz.sh in the main project directory to visualize the resulting
!! table using gnuplot
!!
!! This version uses explicit loops through the array elements
!!
program liebmann_example
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

    call liebmann(surface)

    do i = 1, SIZE; print *, surface(i, :); end do
end subroutine

subroutine liebmann(surface)
    real, dimension(SIZE, SIZE), intent(out) :: surface
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
    real, dimension(SIZE, SIZE), intent(out) :: surface

    surface          = 0.0
    surface(1, :)    = BOUNDARY_VALUE
    surface(SIZE, :) = BOUNDARY_VALUE
    surface(:, 1)    = BOUNDARY_VALUE
    surface(:, SIZE) = BOUNDARY_VALUE
end subroutine

function iterate(prev, next) result(delta)
    real, dimension(SIZE, SIZE), intent(in)  :: prev
    real, dimension(SIZE, SIZE), intent(out) :: next
    real :: delta

    integer :: i, j

    delta = 0.0
    do j = 2, SIZE-1
        do i = 2, SIZE-1
            next(i,j) = &
                (prev(i-1, j) + &
                 prev(i+1, j) + &
                 prev(i, j-1) + &
                 prev(i, j+1)) / 4.0
             delta = max(delta, abs(next(i,j)-prev(i,j)))
        end do
    end do
end function

end program
