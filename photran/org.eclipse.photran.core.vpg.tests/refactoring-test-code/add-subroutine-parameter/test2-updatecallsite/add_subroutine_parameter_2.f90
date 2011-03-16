program updatecallsite ! Tests that the call site is updated appropriately - basic.
    implicit none
    call sub(2)
end program updatecallsite
subroutine sub(z) !<<<<< 5, 1, 5, 5, integer; intent(in) :: y, 0, 0, pass
    integer, intent(in) :: z
end subroutine
