module module1
    implicit none
    integer :: accessed_variable = 1, assigned_variable, unused_variable
    integer, private :: private_variable
    private :: private_subroutine
contains
    subroutine called_subroutine; end subroutine
    subroutine unused_subroutine; end subroutine
    subroutine private_subroutine; end subroutine
end module module1
