program reverseNested2

    integer i, j

    do i=1,10

        !<<<<<8,1,10,1,pass
        do j=2,8,2

            print *, i
        end do
    end do

end program reverseNested2
