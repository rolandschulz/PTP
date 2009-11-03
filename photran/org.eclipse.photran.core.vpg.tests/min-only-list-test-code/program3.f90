module m
   integer :: used, unused
end module

program modules
    use m, only: whatever => used, unused
    implicit none

    whatever = 3
end program modules
