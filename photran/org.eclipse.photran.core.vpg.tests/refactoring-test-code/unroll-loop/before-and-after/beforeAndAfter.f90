program test

    implicit none

    integer :: i, j

    j=1
    !<<<<<8,1,13,1,1,true,pass
    do i=1,5
        print *, i
        j = i
    end do
    j = 1

end program test
