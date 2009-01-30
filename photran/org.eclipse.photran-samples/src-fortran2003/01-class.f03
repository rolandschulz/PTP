! Simple class -- JO 1/28/09
module m
	implicit none

	type c
	contains
		procedure :: p
	end type
contains
	subroutine p(self)
		class(c), intent(in) :: self
		print *, 'Hi'
	end subroutine
end module

program Fortran2003
	use m
	implicit none

	type(c) :: t
	call t%p
	stop
end program
