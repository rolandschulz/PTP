! Test 12: Valid ASSIGN and comment GOTO statement
! Test passes and only replaces the assign because the goto is commented out
program valid_assign_comment_goto !<<<<< 1, 1, 11, 12, true, pass

    assign 100 to label
    !goto 1000
100 stop

1000 print *, "Should not get here"
    goto 100
end program
