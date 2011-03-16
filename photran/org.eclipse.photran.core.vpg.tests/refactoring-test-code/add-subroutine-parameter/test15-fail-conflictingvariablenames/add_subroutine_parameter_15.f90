program conflictingvariablenames ! Tests that a parameter cannot be given the same name as another in scope.

end program conflictingvariablenames
subroutine sub(z) !<<<<< 4, 1, 4, 5, integer; intent(in) :: alreadyused, 0, 0, fail-final
	real :: alreadyused
    integer, intent(in) :: z
end subroutine
