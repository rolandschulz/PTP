program test

    implicit none

    integer :: i, j
    integer, dimension(50,60) :: A

    !<<<<<8,1,14,1,5,20,pass
    do i=1,50
        do j=1,60
            A(i,j) = i+j
        end do
    end do


end program test
