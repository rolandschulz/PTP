! Test Case 23 1 ASSIGN Label, goto 2 different addresses, without the default case
! Test passes but does not include the default case in the two select case statements
program one_label_goto_two_different_addresses !<<<<< 1, 1, 20, 12, false, pass

    assign 100 to label
    goto 1000

100 assign 200 to label
    goto 2000

200 stop

1000 print *, "First goto reaches here"
    goto label

2000 print *, "Second goto reaches here"
    goto label

3000 print *, "Fall-through"
end program
