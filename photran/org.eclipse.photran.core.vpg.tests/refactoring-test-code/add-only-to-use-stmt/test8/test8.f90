program test8 !precondition test - have 2 declarations of subroutine in same project
    use module4 !<<<<< 2, 9, 2, 16,, pass
    implicit none

    call help_common4
end program test8

subroutine help_common4
    implicit none
    real blah
end subroutine
