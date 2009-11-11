subroutine main()
	integer :: i, j 
	
	!<<<<<START
	do j = 1, 10
		do i = 1, 10
			print *, i+j
		end do
	end do
	!<<<<<END

end subroutine