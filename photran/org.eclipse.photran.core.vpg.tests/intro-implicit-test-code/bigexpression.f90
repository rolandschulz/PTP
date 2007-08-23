program p
	integer :: hello
contains
	subroutine s
		parameter (THREE = 3)
		print *, THREE + 2 + 63 * twice(4)
	end subroutine
	
	integer function twice(n)
		intent(in) :: n
		twice = 2 * n
	end function
end program