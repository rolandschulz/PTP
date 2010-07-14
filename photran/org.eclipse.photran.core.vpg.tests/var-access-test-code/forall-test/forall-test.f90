program forall
    implicit none
    integer :: i, arr(100)
    forall (i = 1:10)
    !<<<<< 4,13,FORALL
        arr(i) = i
        !<<<<< 6,9,WRITE
        !<<<<< 6,13,READ
        !<<<<< 6,18,READ
    end forall
    print *, arr
    !<<<<< 11,14,READ
end program forall