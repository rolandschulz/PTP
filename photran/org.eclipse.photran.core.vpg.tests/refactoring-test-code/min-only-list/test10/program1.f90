module m
   integer :: used, unused
end module

program modules
    use m !<<<<< 6, 9, 6, 10, pass
    implicit none

    used = 3
end program modules
