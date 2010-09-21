program test

    implicit none

    integer :: i, j

    !<<<<<7,1,13,1,5,20,pass
    do i=1,50
        do j=1,i
            print *, i
        end do
    end do


end program test
