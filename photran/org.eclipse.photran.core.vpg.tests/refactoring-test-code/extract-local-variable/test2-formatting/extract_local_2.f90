program extract_local
    implicit none
    real :: numbers(5) = (/ 1.2, 3.4, 5.6, 7.8, 9.0 /)
    print *, sum(numbers)    &
                  /          &
             size(numbers,1) !<<<<< 6, 14, 6, 29, integer :: count, true
end program
