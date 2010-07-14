program testblock
	integer :: x=4

	block
		integer :: x=2

		print *, x
	end block

	print *, x
end program testblock !<<<<< 11