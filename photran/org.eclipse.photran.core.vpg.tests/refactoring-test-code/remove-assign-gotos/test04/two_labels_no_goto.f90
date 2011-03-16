! Test 4: 2 ASSIGN Labels, No GOTO statement
! Test passes and replaces two assign statements with assignments 
program two_labels_no_goto !<<<<< 1, 1, 7, 12, true, pass
    assign 100 to label1
    assign 325 to label2
100 continue
325 stop
end program
