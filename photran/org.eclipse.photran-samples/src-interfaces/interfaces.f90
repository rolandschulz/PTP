!!
!! Examples of using INTERFACE blocks in Fortran
!!
!! INTERFACE blocks have two different uses:
!!
!!     1. Declaring the interface of an external subprogram
!!        (as an alternative to the EXTERNAL statement, which
!!        declares that the subprogram is external but says nothing
!!        about its signature)
!!
!!     2. Overloading
!!
!! J. Overbey 9/15/08
!!
module interface_example
    implicit none

    ! USAGE 1: Declaring the interface of an external subprogram

    ! This declares two functions in the module, named "one" and "two"
    ! They will be resolved to the corresponding external functions at link time
    interface
        integer function one()
        end function

        integer function two()
        end function
    end interface

    ! Since "one" and "two" are in the module, they can be made private; we'll
    ! hide "two" from the outside world
    private :: two

    ! USAGE 2: Overloading

    ! This declares a function named "description" which can be called with
    ! either an integer or real parameter;
    !     description(1)
    ! will actually invoke description_int(1), while
    !     description(2.0)
    ! will actually invoke description_real(2.0)
    interface description
        ! This overload is an external function; we'll use the same syntax as
        ! above, simply declaring its interface
        character(len=50) function description_int(value)
            integer, intent(in) :: value
        end function description_int

        ! This overload is a procedure in the module, so we can use a
        ! MODULE PROCEDURE statement since we already know its interface
        module procedure description_real
    end interface

    ! INTERFACE blocks can also be used for operator overloading, but I won't
    ! demonstrate that here

contains
    character(len=50) function description_real(value)
        real, intent(in) :: value
        write (description_real, *) "REAL:", value
    end function
end module interface_example

program program
    use interface_example, only: one, description
    implicit none
    integer :: two = 2  ! Note that "two" is hidden in the module,
                        ! so we can declare a "two" variable here

    ! Call the function declared in the INTERFACE block
    print *, one()

    ! Call the overloaded "description" function
    print *, description(two)
    print *, description(3.0)
end program program

!! EXTERNAL SUBPROGRAMS
integer function one(); one = 1; end function
integer function two(); two = 2; end function
character(len=50) function description_int(value)
    implicit none
    integer, intent(in) :: value
    write (description_int, *) "INTEGER:", value
end function
