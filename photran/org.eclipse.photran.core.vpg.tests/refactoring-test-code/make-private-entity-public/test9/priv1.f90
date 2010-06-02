program priv1
    implicit none
    integer a, b, c !<<<<< 3, 16, 3, 17, fail-initial
    private a, c
end program priv1
