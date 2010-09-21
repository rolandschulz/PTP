program test

    implicit none

    integer :: i, n=4
    !<<<<<6,1,8,1,2,false,pass
    do i=1,n*n
        print *, i
    end do

end program test
