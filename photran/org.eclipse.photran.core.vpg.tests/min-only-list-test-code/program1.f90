module m
   integer :: used, unused
end module

program modules
    use m
    implicit none

    used = 3
end program modules
