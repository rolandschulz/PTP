! Test Case 22 1 ASSIGN Label, 1 Goto Address No Default Case
! Test passes but does not include a default case in the select case statement
program one_label_one_address !<<<<< 1, 1, 12, 12, false, pass

    assign 100 to label
    goto 1000
100   stop

! Here is the intended "subroutine"
1000 print *, "hello"
    goto label
end program
