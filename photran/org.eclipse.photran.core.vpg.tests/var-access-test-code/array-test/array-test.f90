program arraytest
    character(len=5), dimension(10) :: array
    integer :: i
    
    do i=1,5
        array(i)(3:4) = "OK";
        !<<<<< 6,9,WRITE
        !<<<<< 6,15,READ
    end do
end program