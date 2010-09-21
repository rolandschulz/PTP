program test

    implicit none

    integer :: i, j=-1
    !<<<<<6,1,13,1,1,true,pass
    do i=1,5,2
        j = j**i
        j = j+j
        print *, i
        print *, j
    end do

end program test
