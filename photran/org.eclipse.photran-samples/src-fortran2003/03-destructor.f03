! Destructor -- JO 1/28/09
module m1
	implicit none
	private

	type, public :: c1
		character(20) :: STR = 'Hey there'
	contains
		procedure :: p
		final :: destructor
	end type
contains
	subroutine p(self)
		class(c1), intent(in) :: self
		print *, 'Hi'
	end subroutine

	subroutine destructor(self)
		type(c1), intent(in) :: self
		print *, 'Bye'
	end subroutine
end module

module m2
	use m1
	implicit none
	private

	type, extends(c1), public :: c2
	contains
		procedure :: p
	end type
contains
	subroutine p(self)
		class(c2), intent(in) :: self
		print *, self%STR
	end subroutine
end module

program Fortran2003
	use m1
	use m2
	implicit none

	type(c1), target :: c1obj
	type(c2), target :: c2obj

	class(c1), pointer :: c1ptr => null()
	class(c2), pointer :: c2ptr => null()

	allocate(c1ptr)
	allocate(c2ptr)

	c1ptr => c1obj
	c2ptr => c2obj

	call c1ptr%p
	call c2ptr%p
	c1ptr => c2ptr
	call c1ptr%p

	deallocate(c1ptr)
	deallocate(c2ptr)

	stop
end program
