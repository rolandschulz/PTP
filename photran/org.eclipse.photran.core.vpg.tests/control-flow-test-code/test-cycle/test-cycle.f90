program testcycle
	integer :: i

	do i=1,5
		print *, "1"
		cycle
		print *, "2"
	end do
	
	print *, "3"

end program testcycle !<<<<< 12