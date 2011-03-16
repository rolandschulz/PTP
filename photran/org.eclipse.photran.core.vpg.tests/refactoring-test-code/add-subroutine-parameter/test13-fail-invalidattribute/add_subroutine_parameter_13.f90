program invalidvalidattributes ! Tests that invalid attributes fail

end program invalidvalidattributes
subroutine sub(z) !<<<<< 4, 1, 4, 5, integer; intent(inoot) :: y, 0, 0, fail-final
    integer, intent(in) :: z
end subroutine
