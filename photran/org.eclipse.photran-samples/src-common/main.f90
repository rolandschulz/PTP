!!
!! Demonstrates COMMON block hell
!!
!! J. Overbey 8/18/08
!!
program main
    implicit none

    external change_c

    common a, b
    integer :: a
    real    :: b

    common /common/ c, d, e /common2/ f, g, /common3/ h
    complex :: c
    dimension c(3)
    ! double precision :: d, e, f, g, h
    double precision :: d
    double precision :: e
    double precision :: f
    double precision :: g
    double precision :: h

    a = 1
    b = 2.3
    c = (4,5)
    print *, a, b, c

    call change_ab
    call change_c

    print *, "Now the first two variables below should have garbage values"
    print *, "and the third should be (5,6):"
    print *, a, b, c
end program

subroutine change_ab
    implicit none

    ! q and r are stored at the same place in memory as a and b above,
    ! but they have the wrong types
    common q, r
    real    :: q
    integer :: r

    q = 4.5
    r = 6
    print *, q, r
end subroutine
