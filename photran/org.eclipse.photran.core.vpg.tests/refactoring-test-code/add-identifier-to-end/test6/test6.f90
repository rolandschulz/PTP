module testmodule
	integer :: xfromtestmodule
10 end

function testfunction(A)
	integer, intent(in) :: A
	testfunction = 4;
20 end ! Hey this is a comment in a weird spot!!!

!<<<<< 1,1,pass

	print *, "Main program!"

contains
	integer function testfunction(A)
		integer, intent(in) :: A
		testfunction = 4;
30	end function
	subroutine do_stuff
		print *,"Hi!"
40	end subroutine
! I am a big fan of printing greetings
end

subroutine do_stuff

	print *,"Hi!"
! What a weird place to put a comment, right?
50 end