! Inheritance -- JO 1/28/09
module m1
	implicit none

	type c
		character(20) :: STR = 'Hey there'
	contains
		procedure :: p
	end type
contains
	subroutine p(self)
		class(c), intent(in) :: self
		print *, 'Hi'
	end subroutine
end module

module m2
	use m1
	implicit none

	type, extends(c) :: c2
	contains
		procedure :: p => c2_p
	end type
contains
	! Can't call this p
	subroutine c2_p(self)
		class(c2), intent(in) :: self
		print *, self%STR
	end subroutine
end module

program Fortran2003
	use m2
	implicit none

	type(c2) :: t
	call t%p
	stop
end program
