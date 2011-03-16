program whitespacedefaultvalue ! Tests that the refactoring does not allow the default value to be white space

end program whitespacedefaultvalue
subroutine sub(z) !<<<<< 4, 1, 4, 5, integer; intent(in) :: y, 0,    	, fail-final
    integer, intent(in) :: z
end subroutine
