! Checks for replacement of a PAUSE statement
! with PRINT and READ while PAUSE statement is in
! a nested DO/IF block to make sure there are no
! dependencies on surrounding code.

PROGRAM pause_nested_in_if_and_do_loop
  INTEGER :: i
  DO i = 1, 100
    IF (i == 50) THEN
      PAUSE 'mid job'	!<<<<< 10, 7, 10, 21, pass
    END IF
  END DO
  PRINT *, 'i=', i
END PROGRAM pause_nested_in_if_and_do_loop
