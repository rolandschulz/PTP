! Test 1: 1 ASSIGN Label, No GOTO statement
! This test passes and replaces the assign statement with an assignment
program one_label_no_goto !<<<<< 1, 1, 6, 12, true, pass
    assign 100 to label
100 stop
end program
