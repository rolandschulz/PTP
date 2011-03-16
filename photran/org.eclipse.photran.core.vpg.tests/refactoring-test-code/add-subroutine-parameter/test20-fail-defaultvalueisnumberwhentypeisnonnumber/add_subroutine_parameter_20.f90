program defaultvalueisnumberfornonnumbertype ! Tests that non number types cannot be given a number as a default value.

end program defaultvalueisnumberfornonnumbertype
subroutine sub(z) !<<<<< 4, 1, 4, 5, logical :: y, 0, 1, fail-final
    integer, intent(in) :: z
end subroutine
