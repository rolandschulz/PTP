program testforall
	integer, dimension(10) :: array

	forall (I = 1:5)
		array(I) = 2
		array(I) = array(I) * 2
	end forall
	
end program testforall !<<<<< 9