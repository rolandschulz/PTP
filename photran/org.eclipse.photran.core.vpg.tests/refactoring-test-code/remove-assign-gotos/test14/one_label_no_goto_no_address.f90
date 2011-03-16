! Test 14: 1 ASSIGN Label, No GOTO statement, No address
! Test fails because there is no statement 100
program one_label_no_goto_no_address !<<<<< 1, 1, 5, 12, true, fail-initial
    assign 100 to label
end program
