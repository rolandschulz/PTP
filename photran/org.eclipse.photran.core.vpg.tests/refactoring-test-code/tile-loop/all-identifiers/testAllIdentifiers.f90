program test

    implicit none

    integer :: i
    integer :: j
    integer :: n=1, m=20, p=10

    !<<<<<9,1,15,1,20,3,pass
    do i=n,10
        do j=n,m
            print *, i
        end do
    end do

end program test
