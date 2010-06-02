module test5
    implicit none
    integer, private :: attrs !<<<<< 3, 25, 3, 30, pass
end module test5

program p
	use test5
end program p
