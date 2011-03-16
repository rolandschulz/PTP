! Outer GOTO targets selected END IF so insert
! a CONTINUE statement and retarget its label.
! Remove END IF label since no inner GOTO
! statement exists

PROGRAM test_branch_end_if_label
   INTEGER :: k, i
   READ(*,*) k
   IF (k.lt.10) THEN
     GOTO 20
   END IF
   i = k - 10
   IF (i.gt.100)  THEN
     i = i - 100
20 END IF  !<<<<< 15, 4, 15, 9, pass
   PRINT *, i
END PROGRAM test_branch_end_if_label