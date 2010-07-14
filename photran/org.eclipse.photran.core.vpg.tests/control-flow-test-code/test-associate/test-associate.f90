program testassociate
	type :: weights
		integer :: onEarth
	end type

	type :: person
		integer :: age
		type(weights) :: weight
	end type

	type(person) :: me

	me%age = 23

	me%weight%onEarth = 150

	associate (myWeight => me%weight)

		print *, myWeight%onEarth

	end associate

	print *, "I am a heavy boy!"

end program testassociate !<<<<< 25