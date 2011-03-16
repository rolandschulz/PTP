! Test 8:  2 ASSIGN Labels, goto same address
! Test passes, creating two select case statements
program two_labels_same_address !<<<<< 1, 1, 20, 12, true, pass

    assign 100 to label1
    goto 1000
100   print *, "Return to here"
    stop

    assign 100 to label2
    goto 2000

! Here is the intended "subroutine"
1000 print *, "This code will get executed"
    goto label1

2000 print *, "This will not get executed since we return back since the other case stops"
    goto label2

end program
