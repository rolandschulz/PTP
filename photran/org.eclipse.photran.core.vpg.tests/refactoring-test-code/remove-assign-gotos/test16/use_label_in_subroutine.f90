! Test Case 16 Labels Used in a Subroutine
! Test fails because a subroutine is an action statement
program use_label_in_subroutine !<<<<< 1, 1, 15, 4, true, fail-final
    implicit none
    integer label
    assign 20 to label
    goto label
20  call print_label(label)

end program use_label_in_subroutine

subroutine print_label( label1 )
    integer :: label1
    print *,label1
end
