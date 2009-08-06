subroutine main()
	integer :: i, j
	integer :: k, z
	
	!<<<<<START
	do j = 1, 10
		do i = 2, 15
			print *, i                   
       		if (i .gt. j) then
         		print *, i * 10
       		end if                      
       		print *, i
		end do
	end do
	!<<<<<END

end subroutine