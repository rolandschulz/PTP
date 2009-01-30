!>
!! Definition of a string class for Fortran.
!!
!! Jeff Overbey (1/29/09)
!<
module string_module
    implicit none
    private

    type, public :: string
        private
        character, dimension(:), allocatable :: data
        integer :: length = 0
    contains
        procedure :: initialize
        procedure :: value

        generic   :: operator(+) => concat1, concat2
        generic   :: concat => concat1, concat2  ! Photran doesn't parse - R1207
        procedure, private :: concat1
        procedure, private :: concat2

        generic   :: write(formatted) => write_formatted
        procedure, private :: write_formatted

        generic   :: write(unformatted) => write_unformatted
        procedure, private :: write_unformatted

        final     :: destructor
    end type string

contains

    subroutine initialize(self, data)
        class(string), intent(inout) :: self
        character(len=*), intent(in) :: data
        integer :: i

        self%length = len(data)
        allocate(self%data(self%length))
        do i = 1, len(data)
        	self%data(i) = data(i:i)
       	end do
    end subroutine

    ! XLF parses class(string) but gives semantic error; Photran doesn't parse
    type(string) function concat1(self, other)
    	class(string), intent(in) :: self
    	class(string), intent(in) :: other

    	concat1 = concat2(self, other%value())
    end function

    type(string) function concat2(self, other)
    	class(string), intent(in) :: self
    	character(len=*), intent(in) :: other
    	integer :: i

    	concat2%length = self%length + len(other)
        allocate(concat2%data(concat2%length))
        concat2%data(1:self%length) = self%data
        do i = 1, len(other); concat2%data(i + self%length) = other(i:i); end do
    end function

    subroutine write_formatted(self, unit, iotype, v_list, iostat, iomsg)
    	class(string), intent(in) :: self
    	integer, intent(in) :: unit
    	character(*), intent(in) :: iotype
    	integer, dimension(:), intent(in) :: v_list
    	integer, intent(out) :: iostat
    	character(*), intent(inout) :: iomsg
		! TODO Incomplete
    	write (unit=unit, fmt=*, iostat=iostat, iomsg=iomsg) self%value()
    end subroutine

    subroutine write_unformatted(self, unit, iostat, iomsg)
    	class(string), intent(in) :: self
    	integer, intent(in) :: unit
    	integer, intent(out) :: iostat
    	character(*), intent(inout) :: iomsg
		! TODO Incomplete
    	write (unit=unit, fmt=*, iostat=iostat, iomsg=iomsg) self%value()
    end subroutine

    character(len=self%length) function value(self) result(result)
        class(string), intent(in) :: self
        integer :: i

        do i = 1, ubound(self%data, 1)
            result(i:i) = self%data(i)
        end do
    end function

    subroutine destructor(self)
        type(string), intent(inout) :: self
    end subroutine

end module string_module

program string_test
	use string_module
	implicit none

	type(string) :: s1, s2

    print *, "Expected: "
    print *, "Actual:   ", s1%value()

	call s1%initialize('Hello')
	call s2%initialize('!')

	print *, "Expected: Hello"
    print *, "Actual:   ", s1%value()

	s1 = s1%concat(', world')
	print *, "Expected: Hello, world"
    print *, "Actual:   ", s1%value()

	s1 = s1%concat(s2)
	print *, "Expected: Hello, world!"
    print *, "Actual:   ", s1%value()

    s1 = s1 + '!'
	!print *, (s1 + '!')%value()
	print *, "Expected: Hello, world!!"
    print *, "Actual:   ", s1%value()

	print *, "Expected: Hello, world!!"
    print *, "Actual:   ", s1

	print *, "Done"
end program string_test
