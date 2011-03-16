! USER STORY 4, TEST 5
! Adds SAVE attribute to the declaration statements for variables sum1 and sum2
! initialized through the same DATA statement

! EXAMPLE FROM USER STORY

PROGRAM MyProgram
  CALL MySub
  CALL MySub
END PROGRAM MyProgram
 !<<<<< 1,1, pass
SUBROUTINE MySub
  INTEGER :: sum1, sum2
  DATA sum1 /100/, sum2 /200/
  sum1 = sum1 + sum1
  sum2 = sum2 + sum2
  PRINT *, 'sum1=', sum1
  PRINT *, 'sum2=', sum2
END SUBROUTINE MySub
