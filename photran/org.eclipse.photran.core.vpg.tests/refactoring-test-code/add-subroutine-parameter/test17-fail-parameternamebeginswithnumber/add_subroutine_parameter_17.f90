program parameternamebeginswithnumber ! Tests that the variable name cannot begin with a number

end program parameternamebeginswithnumber
subroutine sub(z) !<<<<< 4, 1, 4, 5, integer :: 1y, 0, 0, fail-final
    integer, intent(in) :: z
end subroutine
