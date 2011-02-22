program main
    implicit none

    integer :: i,j


    do i = 1,10
        do j=1,10
            print *,i+j
        100 continue !<<<<< 1, 1, 14, 17, pass
        end do
    end do

end program main
