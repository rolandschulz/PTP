!>
!! Illustrates a bug in IBM XL Fortran for AIX, V12.1 (5724-U82)
!!
!! XLF reports errors when a subclass, whose parent class is in a different
!! module, overrides a procedure with a pointer to class(*) as either an
!! argument or a return type.  If both modules are combined into a single
!! module, the code compiles successfully.
!!
!! Jeff Overbey (2/24/09)
!<
module mod_base
    implicit none
    private
    ! Abstract class "base" contains two routines
    ! * accept_any accepts a pointer to class(*)
    ! * return_any returns a pointer to class(*)
    type, public, abstract :: base
    contains
        procedure(accept_any), deferred :: accept_any
        procedure(return_any), deferred :: return_any
    end type
    abstract interface
        subroutine accept_any(self, arg)
            import base
            class(base), intent(in) :: self
            class(*), pointer, intent(in) :: arg
        end subroutine
        function return_any(self) result(return)
            import base
            class(base), intent(in) :: self
            class(*), pointer :: return
        end function
    end interface
end module
module mod_extended
    use mod_base
    implicit none
    private
    type, public, extends(base) :: extended
    contains
        procedure :: accept_any => extended_accept_any  ! XLF reports Dummy argument arg of overridden binding accept_any and the corresponding dummy argument of overriding binding accept_any must have the same type and type parameters.
        procedure :: return_any => extended_return_any  ! XLF reports The function results of the overridden binding return_any and overridding binding return_any must have the same type and type parameters.
    end type
contains
    subroutine extended_accept_any(self, arg)
        class(extended), intent(in) :: self
        class(*), pointer, intent(in) :: arg
    end subroutine
    function extended_return_any(self) result(return)
        class(extended), intent(in) :: self
        class(*), pointer :: return
        return => null()
    end function
end module
program p; end program
