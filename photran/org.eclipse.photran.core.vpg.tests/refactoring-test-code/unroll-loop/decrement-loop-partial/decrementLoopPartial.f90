program test

    implicit none

    integer :: k
    !<<<<<6,1,9,1,2,false,pass
    do k=9,1,-2
        print *, k
    end do


end program test
