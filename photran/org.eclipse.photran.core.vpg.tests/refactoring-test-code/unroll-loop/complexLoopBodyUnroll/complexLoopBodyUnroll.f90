!Fix this text case after reindenter update
program test

    implicit none

    integer :: i, j=-1
    !<<<<<7,1,14,1,4,false,pass
    do i=1,5
        j = j**i
        j = j+j
        print *, i
        print *, j
    end do

end program test
