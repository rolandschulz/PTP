module common2
    implicit none

    contains
    subroutine help_common2
        common /block/ d, e, f
        integer :: d
        real :: e
        double precision :: f
    end subroutine help_common2
end module common2
