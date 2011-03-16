program negativeposition ! Tests that the refactoring fails with negative positions requested.

end program negativeposition
subroutine sub(z) !<<<<< 4, 1, 4, 5, integer; intent(in) :: y, -1, 0, fail-final
    integer, intent(in) :: z
end subroutine
