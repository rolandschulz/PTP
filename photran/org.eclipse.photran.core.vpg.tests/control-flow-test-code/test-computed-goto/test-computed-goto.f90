program test20
	integer :: x = 2

	go to (10,20) x

	print *, "0"

10	print *, "1"

	return

20	print *, "2"

end program test20 !<<<<< 14