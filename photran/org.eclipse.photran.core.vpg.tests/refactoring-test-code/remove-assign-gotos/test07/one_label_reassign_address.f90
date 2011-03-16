! Test 7: 1 ASSIGN Label, Change value on label 
! Test passes and changes the assign statements to one variable to assignments
program one_label_reassign_address !<<<<< 1, 1, 8, 12, true, pass
    assign 100 to label1
    assign 200 to label1
200 continue
100 stop
end program
