! When two labeled END IF blocks the outer GOTO that 
! targets the selected END IF block should be retargetted
! to a new CONTINUE statement and the original END IF label
! removed but other labeled END IF should remain.

PROGRAM test_end_if_label_between_branch_to_end_if
   INTEGER :: k, i
   READ(*,*) k
   IF (k.lt.10) THEN
     GOTO 30
20 END IF
   i = k - 10
   IF (i.gt.100)  THEN
     i = i - 100  
30 END IF	!<<<<< 15, 1, 15, 9, pass
   PRINT *, i
END PROGRAM test_end_if_label_between_branch_to_end_if