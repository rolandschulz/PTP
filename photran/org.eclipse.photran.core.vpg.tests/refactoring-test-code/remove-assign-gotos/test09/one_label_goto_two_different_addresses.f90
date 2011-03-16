! Test 9:  1 ASSIGN Label, goto 2 different addresses
! Test passes with two select case statements for the single label
program one_label_goto_two_different_addresses !<<<<< 1, 1, 17, 12, true, pass

    assign 100 to label
    goto 1000

100 assign 200 to label
    goto 2000

200 stop

1000 print *, "First goto reaches here"
    goto label

2000 print *, "Second goto reaches here"
    goto label
end program
