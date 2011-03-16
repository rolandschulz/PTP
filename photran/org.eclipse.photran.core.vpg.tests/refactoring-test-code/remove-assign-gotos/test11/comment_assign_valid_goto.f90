! Test 11: Comment ASSIGN have a valid GOTO statement
! Test fails because the assign statement is commented out
program comment_assign_valid_goto !<<<<< 1, 1, 11, 12, true, fail-initial

    !assign 100 to label
    goto 1000
100 stop

1000 print *, "We will get here"
    goto 100
end program
