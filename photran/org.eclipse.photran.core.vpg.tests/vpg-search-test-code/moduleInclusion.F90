PROGRAM hi
!this should work


  USE moduleZiggurat
  IMPLICIT NONE

  REAL ::  x
  INTEGER :: i

  DO  i = 1, 10000
     x = uni( )
  END DO
  WRITE(*,*) x

END PROGRAM hi

