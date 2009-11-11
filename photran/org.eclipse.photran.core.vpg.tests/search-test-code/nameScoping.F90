
SUBROUTINE pgmName
	PRINT *,'Hello'
END SUBROUTINE pgmName



PROGRAM pgmName
!this should work


  USE pgmName
  IMPLICIT NONE

  REAL ::  x
  INTEGER :: i

  DO  i = 1, 10000
     x = hi( )
  END DO
  WRITE(*,*) x
  
  CALL pgmName

END PROGRAM pgmName
