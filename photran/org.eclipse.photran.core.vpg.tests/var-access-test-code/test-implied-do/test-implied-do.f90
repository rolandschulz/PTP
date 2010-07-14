program testimplieddo
    integer :: i,j
    integer, parameter :: sizeY = 10
    integer, dimension(10) :: y
    integer, dimension(12) :: x = (/ ((i,i=1,4),j=1,3) /)
    !<<<<< 5,40,READ
    !<<<<< 5,42,IMPLIED_DO
    !<<<<< 5,49,IMPLIED_DO
    
    print *, (i,i=1,10)
    !<<<<< 10,15,READ
    !<<<<< 10,17,IMPLIED_DO

    data (y(i),i=1,10)/sizeY * 7/
    !<<<<< 14,11,READ
    !<<<<< 14,13,READ
    !<<<<< 14,16,IMPLIED_DO
    !<<<<< 14,24,READ
end program testimplieddo
