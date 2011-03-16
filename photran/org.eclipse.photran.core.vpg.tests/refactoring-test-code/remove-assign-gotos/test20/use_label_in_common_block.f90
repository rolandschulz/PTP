! Test Case 20 Label Used in Common Block
! Test passes because a common block is not an action statement
program use_label_in_common_block !<<<<< 1, 1, 11, 38, true, pass
    implicit none
    common /test/ label
    integer label
    assign 20 to label
    goto label

20  continue
end program use_label_in_common_block
