! Test 13: Split assign statement with ampersand
! Test passes by replacing the split-up assigned goto and creating a select case
program split_assign_to_two_lines !<<<<< 1, 1, 13, 12, true, pass

    assign 100 &
    to &
    label
    goto 1000
100 stop

1000 print *, "Subroutine like stuff here"
    goto label
end program
