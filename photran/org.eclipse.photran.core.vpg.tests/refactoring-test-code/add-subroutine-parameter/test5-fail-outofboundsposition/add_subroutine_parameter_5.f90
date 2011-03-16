program outofbounds ! Tests that the refactoring fails for requests to place the new parameter in a position that is out of bounds.

end program outofbounds
subroutine sub(z) !<<<<< 4, 1, 4, 5, integer; intent(in) :: y, 3, 0, fail-final
    integer, intent(in) :: z
end subroutine
