! Test Case 21 Two of the Same Assign Statements
! Test passes and creates one select case statement with one select case 
program two_assign_same_label_withgotos !<<<<< 1, 1, 13, 12, true, pass

    assign 100 to label
    goto 1000
    
100 assign 100 to label
    goto 1000

1000 print *, "Infinite loop"
    goto label
end program
