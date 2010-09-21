program test

    implicit none

    integer :: i
    !<<<<<6,1,10,1,4,false,pass
    do i=1,3+5,1
        print *, i
    end do


end program test
