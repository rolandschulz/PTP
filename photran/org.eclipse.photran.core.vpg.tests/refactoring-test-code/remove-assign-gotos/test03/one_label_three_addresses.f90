! Test 3:  1 ASSIGN Label, 3 goto address
! Test passes and replaces the three assigns and gotos with a select case
! (Modified test case from the user stories)
program one_label_three_addresses !<<<<< 1, 1, 26, 12, true, pass

     character*50 message

     message = "setting initial label address"
     assign 100 to label
     goto 9000

100  message = "changing label addr for the first time"
     assign 200 to label
     goto 9000

200  message = "changing label address 2nd time."
     assign 300 to label
     goto 9000

300  stop

! This is the "subroutine"
9000 print *, message
     goto label

end program
