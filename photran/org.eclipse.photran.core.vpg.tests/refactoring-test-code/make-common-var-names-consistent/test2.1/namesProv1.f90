program namesProv
    implicit none
    common /names/ a, b, c !<<<<< 3, 13, 3, 18, a_hello, b_hello, c_hello, pass
    integer :: a, b, c

    print *, "Test the test case program with given names"
end program namesProv
