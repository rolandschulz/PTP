subroutine main()
	integer :: i, j
	integer :: k, z
	
	!<<<<<START
	do i = 1, 10
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
	!<<<<<END

end subroutine