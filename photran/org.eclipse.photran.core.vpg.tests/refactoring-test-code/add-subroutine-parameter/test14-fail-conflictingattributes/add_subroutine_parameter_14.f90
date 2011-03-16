program conflictingattributes ! Tests that conflicting attributes (two types) fail.

end program conflictingattributes
subroutine sub(z) !<<<<< 4, 1, 4, 5, integer; real; intent(out) :: y, 0, 0, fail-final
    integer, intent(in) :: z
end subroutine
