program invalidtype ! Fails because the type specified is invalid in the declaration line.

end program invalidtype
subroutine sub(z) !<<<<< 4, 1, 4, 5, invalidtype :: y, 0, 0, fail-final
    integer, intent(in) :: z
end subroutine
