module testmodule
	integer :: xfromtestmodule
end

function testfunction(A)
	integer, intent(in) :: A
	testfunction = 4;
end

program fortrantest !<<<<< 1,1,pass

	print *, "Main program!"

contains
	integer function testfunction(A)
		integer, intent(in) :: A
		testfunction = 4;
	end function
	subroutine do_stuff
		print *,"Hi!"
	end subroutine
end

subroutine do_stuff

	print *,"Hi!"

end