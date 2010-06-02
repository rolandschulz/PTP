module test3
    implicit none
    integer, private :: var !<<<<< 3, 25, 3, 26, pass
end module test3

program p
	use test3
end program p
