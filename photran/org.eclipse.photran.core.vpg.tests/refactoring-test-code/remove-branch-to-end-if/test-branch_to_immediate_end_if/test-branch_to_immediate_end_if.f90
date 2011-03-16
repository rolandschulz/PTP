! GOTO statement is inner GOTO for selected END IF
! so no change is expected. By design we warn the
! user that no change will be made.

PROGRAM test_branch_to_immediate_end_if
   INTEGER :: k, i
   READ(*,*) k
   IF (k.lt.10) THEN
     GOTO 20
20 END IF	!<<<<< 10, 1, 10, 9, fail-initial
   i = k - 10
   IF (i.gt.100)  THEN
     i = i - 100  
   END IF
   PRINT *, i
END PROGRAM test_branch_to_immediate_end_if