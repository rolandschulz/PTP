subroutine main()
	integer :: i, j
	integer :: k, z
	

	do j = 1, 10
		!<<<<<START
		do i = 1, 10
			do k = 2, 20
				print *, i                   
	       		if (i .gt. j) then
	         		print *, i * 10
	       		end if                      
	       		print *, i
	       	end do
		end do
		!<<<<<END
	end do


end subroutine