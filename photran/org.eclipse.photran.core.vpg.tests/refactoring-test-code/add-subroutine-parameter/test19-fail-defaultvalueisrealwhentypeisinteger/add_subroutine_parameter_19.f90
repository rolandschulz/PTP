program defaultvalueisrealforinteger ! Tests that an integer cannot be given a real default value.

end program defaultvalueisrealforinteger
subroutine sub(z) !<<<<< 4, 1, 4, 5, integer :: y, 0, 1.7, fail-final
    integer, intent(in) :: z
end subroutine
