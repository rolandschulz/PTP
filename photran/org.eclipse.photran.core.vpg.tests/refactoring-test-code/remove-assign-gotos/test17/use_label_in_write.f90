! Test Case 17 Label Used in Write
! Test fails because a write is an action statement
program use_label_in_write !<<<<< 1, 1, 11, 31, true, fail-final
    implicit none
    integer label
    assign 20 to label
    goto label
    write (6, label)
20  format("6xTrying specify this line with label")

end program use_label_in_write
