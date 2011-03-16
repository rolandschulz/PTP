! Test if no GOTOs exist in PROGRAM then no change
! will occur from the refactoring so the user is 
! warned and the refactoring halted.

PROGRAM test_no_branch_to_end_if_label
   INTEGER :: k, i
   READ(*,*) k
   IF (k.lt.10) THEN
     PRINT *, k
20 END IF
   i = k - 10
   IF (i.gt.100)  THEN
     i = i - 100  
30 END IF	!<<<<< 14, 1, 14, 9, fail-initial
   PRINT *, i
END PROGRAM test_no_branch_to_end_if_label