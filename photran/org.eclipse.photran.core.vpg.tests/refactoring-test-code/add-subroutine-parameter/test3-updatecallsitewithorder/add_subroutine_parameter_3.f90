program updatecallsitewithorder ! Tests that the position of the parameter is correctly set.
    implicit none
    call sub(2)
end program updatecallsitewithorder
subroutine sub(z) !<<<<< 5, 1, 5, 5, integer; intent(in) :: y, 1, 0, pass
    integer, intent(in) :: z
end subroutine
