program p !<<<<< 1, 9, pass
	integer :: hello
	intrinsic flush
	call s
	call flush; stop
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
