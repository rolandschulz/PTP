! Checks for basic replacement of a PAUSE statement
! with PRINT and READ.

PROGRAM pause_as_only_stmt
   PAUSE 'mid job'	!<<<<< 5, 5, 5, 7, pass
END PROGRAM pause_as_only_stmt
