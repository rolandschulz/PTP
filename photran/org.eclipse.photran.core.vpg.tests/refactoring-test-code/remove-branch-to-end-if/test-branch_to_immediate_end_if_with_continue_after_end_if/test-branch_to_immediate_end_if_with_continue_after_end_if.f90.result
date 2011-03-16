! By design if no GOTOs target the END IF statement even
! if a labels CONTINUE exists no refactoring should take
! place and the user will be notified as such.

PROGRAM branch_to_immediate_end_if_with_continue_after_end_if
   INTEGER :: k, i
   READ(*,*) k
   IF (k.lt.10) THEN
     GOTO 20
20 END IF	!<<<<< 10, 1, 10, 9, fail-initial
30 CONTINUE
   i = k - 10
   IF (i.gt.100)  THEN
     i = i - 100  
   END IF
   PRINT *, i
END PROGRAM branch_to_immediate_end_if_with_continue_after_end_if