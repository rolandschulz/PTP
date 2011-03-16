program nonIfToken_1
	integer :: i, j
	integer :: k, z
	do j = 1, 10 !<<<<< 4, 5, 10, 11, fail-initial
		print *, i                   
   		if (i .gt. j) then
     		print *, i * 10
   		end if                      
   		print *, i
	end do
    
    !!! This tests shows the refactoring failing the initial precondition because the selected text is not a valid
    !!! IF statement or construct, since the first token is "do"
end program