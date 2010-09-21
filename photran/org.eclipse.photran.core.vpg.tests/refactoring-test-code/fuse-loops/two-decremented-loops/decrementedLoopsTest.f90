program test

    implicit none

    integer :: i, j, n = 2
    !<<<<<6,1,10,1,pass
    do i=10,1,-2
        print *, i*i
    end do

    do j= 5,1,-1
        n = n+j
    end do
	print *, n

end program test
