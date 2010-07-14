program testselecttype
    type person
	integer :: age
    end type
    type, extends(person) :: friend
	integer :: duration_of_friendship
    end type

    class(person),pointer :: p
	integer :: i
    type(friend), target :: my_buddy

    p => my_buddy

    select type (p)
		type is (person)
		    print *, "1"
		    print *, "2"
		type is (friend)
		    print *, "3"
		    do i=1,5
		    	print *, "4"
		    	cycle
		    	print *, "5"
		    end do
    end select

    print *, "6"

end program testselecttype !<<<<< 30