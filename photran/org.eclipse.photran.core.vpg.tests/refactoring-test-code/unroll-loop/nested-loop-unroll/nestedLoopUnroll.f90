program test

    implicit none

    integer :: i, j
    !<<<<<6,1,8,1,2,false,pass
    do i=1,5

        do j=1,5
            print *, i+j
        end do
    end do

end program test
