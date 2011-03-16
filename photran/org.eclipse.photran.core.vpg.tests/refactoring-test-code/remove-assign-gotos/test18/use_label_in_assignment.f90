! Test Case 18 Label Used in Assignment
! Test fails because an assignment is an action statement
program use_label_in_assignment !<<<<< 1, 1, 8, 36, true, fail-final
    implicit none
    integer label
    assign 20 to label
    goto label
    label = 40
20  continue
end program use_label_in_assignment
