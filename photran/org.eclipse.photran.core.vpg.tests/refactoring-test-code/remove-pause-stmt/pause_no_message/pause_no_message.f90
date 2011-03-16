! Check when no PAUSE message provided that
! an empty string is inserted in the 
! final refactoring.

PROGRAM pause_no_message
  INTEGER :: i
  DO i = 1, 100
    IF (i == 50) THEN
      PAUSE	!<<<<< 9, 7, 9, 11, pass
    END IF
  END DO
  PRINT *, 'i=', i
END PROGRAM pause_no_message