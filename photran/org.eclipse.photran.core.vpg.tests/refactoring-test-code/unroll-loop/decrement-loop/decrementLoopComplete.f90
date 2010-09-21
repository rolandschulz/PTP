program test

    implicit none

    integer :: k
    !<<<<<6,1,9,1,1,true,pass
    do k=5,1,-1
        print *, k
    end do


end program test
