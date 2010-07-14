program test18
	implicit none
	real :: teststmtfun
	real :: z
	real :: x = 2.

	teststmtfun(z) = z * 2. + 1.

	print *, teststmtfun(x)

end program test18 !<<<<< 11