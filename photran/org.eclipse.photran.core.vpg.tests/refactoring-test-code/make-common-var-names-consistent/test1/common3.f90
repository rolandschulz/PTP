module common3
    implicit none

    contains
    subroutine help_common3
        common /block/ r, s, t
        integer :: r
        real :: s
        double precision :: t

        common /mem/ a, b, c
        integer :: a, b, c
    end subroutine help_common3
end module common3
