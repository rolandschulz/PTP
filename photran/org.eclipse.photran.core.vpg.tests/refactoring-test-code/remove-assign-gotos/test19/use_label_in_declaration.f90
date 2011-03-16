! Test Case 19 Label Used in Declaration
! Test passes because a declaration is not an action statement
program use_label_in_declaration !<<<<< 1, 1, 10, 37, true, pass
    implicit none
    integer label
    assign 20 to label
    goto label

20  continue
end program use_label_in_declaration
