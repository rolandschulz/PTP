program main
	integer :: i, j 
	
	
	do j = 1, 10
		do i = 1, 10 !<<<<< 5, 5, 9, 11, pass
			print *, i+j
		end do
	end do
	

end program main