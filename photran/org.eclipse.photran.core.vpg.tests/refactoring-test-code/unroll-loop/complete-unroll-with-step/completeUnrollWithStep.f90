program test

    implicit none

    integer :: i
    !<<<<<6,1,10,1,1,true,pass
    do i=1,9,2
        print *, i
    end do

end program test
