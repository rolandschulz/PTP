program testloops
	integer :: i,j
	
	firstdo: do i=1,5
		innerdo: do j=1,5
			print *, "1"
			exit firstdo
			print *, "2"
		end do
		print *, "3"
	end do

end program testloops !<<<<< 13