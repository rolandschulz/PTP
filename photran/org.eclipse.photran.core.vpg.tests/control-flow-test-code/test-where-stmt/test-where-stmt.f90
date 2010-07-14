program testwherestmt

	integer, dimension(10) :: array

	do i=1,10
		array(i) = i
	end do

	where (array < 5) array = 1
	
	print *, "2"

end program testwherestmt !<<<<< 13