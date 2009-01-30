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
        generic   :: operator(+) => concat1, concat2
        generic   :: concat => concat1, concat2  ! Photran doesn't parse - R1207
        procedure :: substring
        procedure :: char_at
        procedure :: map
        procedure :: to_uppercase
        procedure :: to_lowercase
        generic   :: write(formatted) => write_formatted	! Photran doesn't parse
        generic   :: write(unformatted) => write_unformatted
        procedure :: value
        final     :: destructor

        procedure, private :: concat1
        procedure, private :: concat2
        procedure, private :: write_formatted
        procedure, private :: write_unformatted
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
    type(string) function concat1(self, other) result(return)
    	class(string), intent(in) :: self
    	class(string), intent(in) :: other

    	return = concat2(self, other%value())
    end function

    type(string) function concat2(self, other) result(return)
    	class(string), intent(in) :: self
    	character(len=*), intent(in) :: other
    	integer :: i

    	return%length = self%length + len(other)
        allocate(return%data(return%length))
        return%data(1:self%length) = self%data
        do i = 1, len(other); return%data(i + self%length) = other(i:i); end do
    end function

    type(string) function substring(self, start, thru) result(return)
    	class(string), intent(in) :: self
    	integer, intent(in) :: start, thru

    	if (thru < start) return

    	return%length = thru - start + 1
        allocate(return%data(return%length))
        return%data(1:self%length) = self%data(start:thru)
    end function

    character(len=1) function char_at(self, index) result(return)
    	class(string), intent(in) :: self
    	integer, intent(in) :: index
    	return = self%data(index)
    end function

    type(string) function map(self, function) result(return)
    	class(string), intent(in) :: self
    	interface
    		character(len=1) function function(string)
    		  character(len=1), intent(in) :: string
    		end function
    	end interface
    	integer :: i

    	return%length = self%length
        allocate(return%data(return%length))
        do i = 1, self%length
         	return%data(i) = function(self%data(i))
        end do
    end function

    type(string) function to_uppercase(self) result(return)
    	class(string), intent(in) :: self
    	return = self%map(to_upper)
    !contains ! Causes XLF compiler bug -- see xlf-bug.f03
    end function
	    character(len=1) function to_upper(ch) result(return)
	    	character(len=1), intent(in) :: ch
	    	integer, parameter :: diff = iachar('a') - iachar('A')

	        if (lle('a', ch) .and. lle(ch, 'z')) then
	        	return = achar(iachar(ch) - diff)
	        else
	        	return = ch
	        end if
		end function
    !end function

    type(string) function to_lowercase(self) result(return)
    	class(string), intent(in) :: self
    	return = self%map(to_lower)
    !contains ! Causes XLF compiler bug -- see xlf-bug.f03
    end function
	    character(len=1) function to_lower(ch) result(return)
	    	character(len=1), intent(in) :: ch
	    	integer, parameter :: diff = iachar('a') - iachar('A')

	        if (lle('A', ch) .and. lle(ch, 'Z')) then
	        	return = achar(iachar(ch) + diff)
	        else
	        	return = ch
	        end if
		end function
    !end function

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

module test_util
	implicit none
contains
	character(len=1) function identity(string) result(return)
		character(len=1), intent(in) :: string
		return = string
	end function

	character(len=1) function always_a(string) result(return)
		character(len=1), intent(in) :: string
		return = 'a'
	end function
end module test_util

program string_test
	use string_module
	use test_util
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

    print *, "Expected: ell"
    print *, "Actual:   ", "Hello, world!!"(2:4)
    print *, "Expected: ell"
    print *, "Actual:   ", s1%substring(2, 4)

    print *, "Expected: He"
    print *, "Actual:   ", s1%substring(0, 2)

    print *, "Expected: "
    print *, "Actual:   ", "Hello, world!!"(5:1)
    print *, "Expected: "
    print *, "Actual:   ", s1%substring(5, 1)

    print *, "Expected: e"
    print *, "Actual:   ", s1%char_at(2)

    print *, "Expected: ", "Hello, world!!"
    print *, "Actual:   ", s1%map(identity)

    print *, "Expected: ", "aaaaaaaaaaaaaa"
    print *, "Actual:   ", s1%map(always_a)

	print *, "Expected: HELLO, WORLD!!"
	print *, "Actual:   ", s1%to_uppercase()

	print *, "Expected: hello, world!!"
	print *, "Actual:   ", s1%to_lowercase()

	print *, "Expected: Hello, world!!"
    print *, "Actual:   ", s1

	print *, "Done"
end program string_test
