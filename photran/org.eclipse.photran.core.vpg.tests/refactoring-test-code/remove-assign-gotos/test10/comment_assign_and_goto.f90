! Test 10: Comment ASSIGN and GOTO statement
! Test fails because the assign and goto statements are commented out
program comment_assign_and_goto !<<<<< 1, 1, 11, 12, true, fail-initial

    !assign 100 to label
    !goto 1000
100 stop

1000 print *, "Should not get here"
    goto 100
end program
