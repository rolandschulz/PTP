! Illustrates various data types in Fortran 90/95.
! J. Overbey 5/18/10
program types
    implicit none

    ! Declare two derived types (equivalent to C structs)
    ! ===================================================

    ! A simple derived type
    type point
        real :: x, y
    end type

    ! A derived type with nested derived types
    type line
        type(point) :: start, end
    end type

    ! Program entrypoint
    ! ==================

    call test_scalars
    call test_scalars_with_kinds
    call test_arrays_1dimension
    call test_arrays_2dimensions
    stop

contains

subroutine test_scalars
    integer          :: integer_var
    real             :: real_var
    double precision :: double_var
    complex          :: complex_var
    logical          :: logical_var
    character(len=5) :: character_var
    type(point)      :: point_var
    type(line)       :: line_var

    integer_var = 1
    real_var = 2.345
    double_var = 6.789012345
    complex_var = ( -1.234, 5.678 )
    logical_var = .true.
    character_var = "Hello"
    point_var = point(12.34, 56.78)
    line_var = line(point(1.2, 3.4), point(5.6, 7.8))

    print *, integer_var
    print *, real_var
    print *, double_var
    print *, complex_var
    print *, logical_var
    print *, character_var
    print *, point_var
    print *, line_var
end subroutine

subroutine test_scalars_with_kinds
    integer(kind=2)  :: integer_2
    integer(kind=4)  :: integer_4
    integer(kind=8)  :: integer_8

    real(kind=selected_real_kind(3))  :: real_3
    real(kind=selected_real_kind(4))  :: real_4
    real(kind=selected_real_kind(7))  :: real_7

    integer_2 = -100
    integer_4 = -1000
    integer_8 = -1000000
    print *, integer_2, integer_4, integer_8

    real_3 = -100.001
    real_4 = -1000.0001
    real_7 = -1000000.0000001 ! gfortran rounds this off
    print *, real_3, real_4, real_7
end subroutine

subroutine test_arrays_1dimension
    implicit none

    real             :: real_array(5)
    type(point)      :: point_array(3)
    type(line)       :: line_array(2)

    real_array = (/ 1.2, 3.4, 5.6, 7.8, 9.0 /)
    point_array = (/ point(12.34, 56.78), &
                   point(-100, -1000),  &
                   point(0, 999)        /)
    line_array = (/ line(point(1.2, 3.4), point(5.6, 7.8)), &
                  line(point(8.7, 6.5), point(4.3, 2.1))  /)

    print *, real_array
    print *, point_array
    print *, line_array
end subroutine

subroutine test_arrays_2dimensions
    implicit none

    integer :: row, col

    real             :: real_matrix(3, 5)
    type(point)      :: point_matrix(3, 5)
    
    ! Fortran stores arrays in *column-major* order,
    ! unlike C.  It also indexes from 1, unlike C.
    ! So real_array(2,5) is the second row, fifth column.

    ! real_matrix is this:
    ! [  1.1  1.2  1.3  1.4  1.5  ]
    ! [  2.1  2.2  2.3  2.4  2.5  ]
    ! [  3.1  3.2  3.3  3.4  3.5  ]
    do col = 1, 5
        do row = 1, 3
            real_matrix(row, col) = row + (col / 10.0)
        end do
    end do
    print *, "real_matrix is:"
    print *, real_matrix
    
    ! point_matrix is this:
    ! [ (1,1) (1,2) (1,3) (1,4) (1,5) ]
    ! [ (2,1) (2,2) (2,3) (2,4) (2,5) ]
    ! [ (3,1) (3,2) (3,3) (3,4) (3,5) ]
    do col = 1, 5
        do row = 1, 3
            point_matrix(row, col) = point(row, col)
        end do
    end do
    print *, "point_matrix is:"
    print *, point_matrix
end subroutine

end program types
