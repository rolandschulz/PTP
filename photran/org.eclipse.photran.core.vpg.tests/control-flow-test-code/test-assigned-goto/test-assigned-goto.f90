program testassignedgoto
	integer :: return_label

	assign 200 to return_label

	goto return_label

	print *, "I will never be printed..."

200 print *, "but I will!"

end program testassignedgoto !<<<<< 12