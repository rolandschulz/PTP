module test6
    implicit none
    integer, private :: attrs, blah !<<<<< 3, 32, 3, 36, pass
end module test6

program p
	use test6
end program p
