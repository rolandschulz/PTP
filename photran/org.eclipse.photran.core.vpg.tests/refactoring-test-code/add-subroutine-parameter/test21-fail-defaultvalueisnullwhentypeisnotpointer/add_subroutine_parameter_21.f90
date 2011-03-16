program defaultvalueisnullfornonpointer ! Tests that null cannot be the default value for a non pointer

end program defaultvalueisnullfornonpointer
subroutine sub(z) !<<<<< 4, 1, 4, 5, integer :: y, 0, null, fail-final
    integer, intent(in) :: z
end subroutine
