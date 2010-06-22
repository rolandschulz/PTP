program reverseLoopWithExpr

    integer i, j
    j = 4

    !<<<<<6,1,8,1,pass
    do i=3,j*j,j

        print *, i
    end do

end program reverseLoopWithExpr
