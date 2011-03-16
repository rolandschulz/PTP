! Refactoring requires that PAUSE statement be
! selected. Check that no refactoring occurs
! when PAUSE is not selected.

PROGRAM pause_not_selected
  INTEGER :: i
  DO i = 1, 100
    IF (i == 50) THEN
      PAUSE 'mid job'
    END IF
  END DO
  PRINT *, 'i=', i	!<<<<< 12, 3, 12, 5, fail-initial
END PROGRAM pause_not_selected
