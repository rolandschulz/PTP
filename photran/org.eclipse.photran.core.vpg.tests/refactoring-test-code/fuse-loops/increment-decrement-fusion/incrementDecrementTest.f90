program test

    implicit none

    integer :: i, j
    !<<<<<6,1,10,1,pass
    do i=2,6
        print *, i
    end do

    do j=6,2,-1
        print *, j
    end do

end program test
