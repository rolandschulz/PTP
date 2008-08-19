subroutine change_c
    implicit none

    ! Now k is the same as c in main.f90
    common /common/ k
    complex :: k

    k = (5,6)
end subroutine
