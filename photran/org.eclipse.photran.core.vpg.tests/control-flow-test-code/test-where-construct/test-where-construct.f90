program testwhereconstruct
	integer, dimension(10) :: array
	
	do i=1,10
		array(i) = i
	end do
	
	where (array > 5)
		array = 2
		array = 3
	elsewhere (array < 2)
		array = 10
	elsewhere (array < 5)
		array = 400
	elsewhere
		array = 1
	end where

end program testwhereconstruct !<<<<< 19