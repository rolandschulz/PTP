PROGRAM MyProgram
    IMPLICIT NONE
    CHARACTER(len=5) :: parmValFormat   ! 3,25
999 FORMAT('(A',I2,',')
    WRITE(parmValFormat,999)  ! 5,11
    WRITE(25, parmValFormat // "' = ', I10, ' [CHANGED]')")  ! 6,15
END PROGRAM MyProgram
