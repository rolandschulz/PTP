module test7_1
    implicit none
    private
    real :: r
    integer five
    real :: blah, ok !<<<<< 6, 13, 6, 17, pass
end module test7_1

program p
	use test7_1
end program p
