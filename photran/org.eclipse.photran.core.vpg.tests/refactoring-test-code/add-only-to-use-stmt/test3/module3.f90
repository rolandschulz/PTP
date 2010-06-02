module module3
    implicit none
    integer :: onlyTest = 2

    contains
    subroutine help_common3
        common /block/ r, s, t
        integer :: r
        real :: s
        double precision :: t

		print *, onlyTest
    end subroutine help_common3
end module module3
