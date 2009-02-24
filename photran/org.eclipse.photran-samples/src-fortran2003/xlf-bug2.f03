! xlf-bug2.f03 - Implementation of the xlf-bug2 class and xlf-bug2_module module
module module
    implicit none

    ! Abstract class "super" contains two routines
    ! * accept_any accepts a pointer to class(*)
    ! * return_any returns a pointer to class(*)
    type, abstract :: super
    contains
        procedure(accept_any), deferred :: accept_any
        procedure(return_any), deferred :: return_any
    end type
    abstract interface
        subroutine accept_any(self, arg)
            import super
            class(super), intent(in) :: self
            class(*), pointer, intent(in) :: arg
        end subroutine
        function return_any(self) result(return)
            import super
            class(super), intent(in) :: self
            class(*), pointer :: return
        end function
    end interface

    type, extends(super) :: base
    contains
        procedure :: accept_any => base_accept_any
        procedure :: return_any => base_return_any
    end type
contains
    subroutine base_accept_any(self, arg)
        class(base), intent(in) :: self
        class(*), pointer, intent(in) :: arg
    end subroutine
    function base_return_any(self) result(return)
        class(base), intent(in) :: self
        class(*), pointer :: return
        return => null()
    end function
end module
program p; end program
