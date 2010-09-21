program test

    implicit none

    integer :: i, j

    !<<<<<7,1,13,1,2,10,pass
    do i=1,40
        do j=i,50
            print *, i
        end do
    end do


end program test
