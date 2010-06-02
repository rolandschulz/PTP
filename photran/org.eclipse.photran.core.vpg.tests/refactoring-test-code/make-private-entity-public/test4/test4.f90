module test4
    implicit none
    real, private :: blah, hi !<<<<< 3, 22, 3, 26, pass
end module test4

program p
	use test4
end program p
