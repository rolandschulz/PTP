program testwhereconstruct
    integer, dimension(10) :: array

    do i=1,10
    !<<<<< 4,8,WRITE
        array(i) = i
        !<<<<< 6,9,WRITE
        !<<<<< 6,15,READ
        !<<<<< 6,20,READ
    end do

    where (array > 5)
    !<<<<< 12,12,READ
        array = 2
        !<<<<< 14,9,WRITE
        array = 3
    elsewhere (array < 2)
    !<<<<< 17,16,READ
        array = 10
        !<<<<< 19,9,WRITE
    elsewhere (array < 5)
    !<<<<< 21,16,READ
        array = 400
    elsewhere
        array = 1
        !<<<<< 25,9,WRITE
    end where

end program testwhereconstruct
