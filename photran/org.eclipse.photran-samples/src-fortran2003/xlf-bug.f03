!>
!! Illustrates a bug in the XLF compiler.  The function "identity" is an internal function which
!! is passed as a parameter to the "map" routine.  However, when it is invoked as a callback,
!! the string it receives as its first parameter is not the string that was actually passed
!! as the first parameter by the invoking function.
!!
!! Jeff Overbey (1/29/09)
!<
module string_module
    implicit none
    private

    type, public :: string
    contains
        procedure :: map
        procedure :: copy
    end type string

contains

	! Applies function to each of the strings 'H', 'e', 'l', 'l', 'o'
    subroutine map(self, function)
    	class(string), intent(in) :: self
    	interface
    		character(len=1) function function(string)
    		  character(len=1), intent(in) :: string
    		end function
    	end interface

		character, dimension(5) :: hello = (/ 'H', 'e', 'l', 'l', 'o' /)
    	integer :: i

		do i = 1, 5
        	print *, "Invoking callback with parameter ", hello(i)
        	hello(i) = function(hello(i))
        end do
    end subroutine

    ! Invokes map with the identity function (defined below)
    subroutine copy(self)
    	class(string), intent(in) :: self
    	call self%map(identity)
    contains
        !!!!! Error is demonstrated when this (internal) function is
        !!!!! passed as an argument to the map procedure: The "ch"
        !!!!! argument is a garbage string, not the one actually
        !!!!! passed by map
	    character(len=1) function identity(ch) result(return)
	    	character(len=1), intent(in) :: ch
            print *, "Callback invoked with parameter ", ch
            return = ch
		end function
    end subroutine
end module string_module

program string_test
	use string_module
	type(string) :: s
	call s%copy()
end program string_test
