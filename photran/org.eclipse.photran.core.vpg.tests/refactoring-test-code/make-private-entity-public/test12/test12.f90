module test
    implicit none
    private
    real :: blah, hi
contains
    subroutine helpMe !<<<<< 6, 16, 6, 22, pass
    end subroutine
end module test

program p
	use test
end program p
