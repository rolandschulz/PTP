module module4
    implicit none
	integer f

    contains
    subroutine help_common4
        common /mem/ a, b, c
        integer :: a, b, c
    end subroutine help_common4
end module module4
