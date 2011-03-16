!Test 2:  1 ASSIGN Label, 1 goto address 
!Test passes and replaces the assigned goto statements with a select case
program one_label_one_address !<<<<< 1, 1, 12, 12, true, pass

    assign 100 to label
    goto 1000
100   stop

! Here is the intended "subroutine"
1000 print *, "hello"
    goto label
end program
