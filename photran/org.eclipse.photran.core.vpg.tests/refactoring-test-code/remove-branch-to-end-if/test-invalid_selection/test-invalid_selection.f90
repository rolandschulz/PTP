! Refactoring requires that a labeled END IF is selected.
! This test checks when user selects something else
! the refactoring should not proceed.

PROGRAM test_invalid_selection
   INTEGER :: k, i
   READ(*,*) k
   IF (k.lt.10) THEN
     PRINT *, k
20 END IF
   i = k - 10
   IF (i.gt.100)  THEN
     i = i - 100  
30 END IF
   PRINT *, i               !<<<<< 15, 4, 15, 14, fail-initial
END PROGRAM test_invalid_selection
