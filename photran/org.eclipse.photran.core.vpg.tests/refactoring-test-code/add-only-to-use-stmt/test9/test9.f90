program main !requires refactoring to add entities already being used in the module to the ONLY list
    use module1 !<<<<< 2, 9, 2, 16,, pass
    implicit none

    assigned_variable = accessed_variable
    call called_subroutine
end program main
