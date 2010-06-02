subroutine main()
	integer :: i, j
	integer :: k, z
	
	
	do i = 1, 10 !<<<<< 6, 5, 16, 11, pass
		do j = 1, 15
			do k = 1,20
				print *, i                   
	       		if (i .gt. j) then
	         		print *, i * 10
	       		end if                      
	       		print *, i
	       	end do
		end do
	end do
	

end subroutine