program testifconstruct
	integer :: x = 2
	
	if (x > 2) then
		print *, "x is greater than 2!"
	else if (x < -2) then
		print *, "x is less than -2!"
		print *, "..."
	else
		print *, "I have no clue what x is!"
	end if
	
	print *, "This print statement is after the if construct!"	

end program testifconstruct !<<<<< 15