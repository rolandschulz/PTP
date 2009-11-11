module m
   integer :: used, unused
end module

program modules
    use m, only: used, unused
    implicit none

    used = 3
end program modules
