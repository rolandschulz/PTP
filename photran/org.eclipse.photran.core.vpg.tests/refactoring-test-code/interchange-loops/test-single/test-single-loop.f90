subroutine main()
	integer :: i, j
	integer :: k, z
	
	
	do j = 1, 10 !<<<<< 6, 5, 12, 11, fail-initial
		print *, i                   
   		if (i .gt. j) then
     		print *, i * 10
   		end if                      
   		print *, i
	end do
	

end subroutine