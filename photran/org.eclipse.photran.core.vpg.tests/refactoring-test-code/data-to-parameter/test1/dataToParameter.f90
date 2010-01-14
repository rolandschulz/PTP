program dataToParameter !<<<<<1,1,true
	implicit none
	real :: x, y, z
	integer :: a, b, c !A comment
	!Another comment on a new line
	data x,y,z/1.,2.,3./ !Those values are assigned
	!About to assign more values
	data a/10/,b/15/,c/20/ !More values are assigned
	!About to change some assigned values
	x = 5.4
	b = 6

end program dataToParameter
