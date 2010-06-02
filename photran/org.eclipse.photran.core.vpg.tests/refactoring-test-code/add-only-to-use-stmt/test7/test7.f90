program test7 !precondition test - have 2 modules in project with same name
    use module4 !<<<<< 2, 9, 2, 16,, fail-initial
    implicit none

end program test7

module module4
    implicit none
    contains
        subroutine help
        end subroutine
end module
