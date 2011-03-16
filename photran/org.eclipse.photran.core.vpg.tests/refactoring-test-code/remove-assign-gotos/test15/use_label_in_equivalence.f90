! Test Case 15 Label Used in Equivalence
! Test passes because equivalence is not an action statement
program use_label_in_equivalence !<<<<< 1, 1, 12, 37, true, pass
    implicit none
    real :: flabel
    integer :: label
    equivalence (label, flabel)
    assign 20 to label
    goto label

20  continue
end program use_label_in_equivalence
