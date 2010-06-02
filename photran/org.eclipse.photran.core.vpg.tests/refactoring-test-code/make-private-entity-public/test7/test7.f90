module test7
    implicit none
    private
    real :: r
    integer five
    real :: blah !<<<<< 6, 13, 6, 17, pass
end module test7

program p
	use test7
end program p
