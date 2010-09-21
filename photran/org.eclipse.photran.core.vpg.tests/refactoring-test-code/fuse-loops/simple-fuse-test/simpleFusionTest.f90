program test

    implicit none

    integer :: i, j
    !<<<<<6,1,10,1,pass
    do i=11,15,1
        print *, i
    end do

    do j=1,5
        print *, j
    end do


end program test
