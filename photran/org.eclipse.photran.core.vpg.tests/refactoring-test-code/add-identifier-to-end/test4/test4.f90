module testmodule
	integer :: xfromtestmodule
end module

function testfunction(A)
	integer, intent(in) :: A
	testfunction = 4;
end function

program fortrantest !<<<<< 1,1,pass

print *, "Main program!"

end program

subroutine do_stuff

	print *,"Hi!"

end subroutine