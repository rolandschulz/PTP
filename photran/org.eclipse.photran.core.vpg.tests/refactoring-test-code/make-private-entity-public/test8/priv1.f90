program priv1
    implicit none
    integer a, b, c !<<<<< 3, 13, 3, 13, fail-initial
    private a, c
end program priv1
