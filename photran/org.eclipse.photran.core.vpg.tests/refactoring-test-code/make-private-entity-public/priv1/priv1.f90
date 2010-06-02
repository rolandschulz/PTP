module priv1
    implicit none
    integer a, b, c
    private a, c !<<<<< 4, 13, 4, 14, pass
end module priv1

program p
	use priv1
end program p
