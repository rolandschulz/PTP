program dontsetvariabletype ! Tests that the default variable type is set to real

end program dontsetvariabletype
subroutine sub(z) !<<<<< 4, 1, 4, 5, intent(in) :: y, 0, 3, pass
    integer, intent(in) :: z
end subroutine
