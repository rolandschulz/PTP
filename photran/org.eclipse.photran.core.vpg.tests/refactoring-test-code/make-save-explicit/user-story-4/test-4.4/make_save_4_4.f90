! USER STORY 4, TEST 4
! Adds SAVE attribute to the declaration statements for variables sum1 and sum2
! initialized through the same DATA statement (alternate arrangement of DATA
! statement)

! EXAMPLE FROM USER STORY

PROGRAM MyProgram
  CALL MySub
  CALL MySub
END PROGRAM MyProgram

SUBROUTINE MySub
  INTEGER :: sum1, sum2
  DATA sum1, sum2 /100, 200/
  sum1 = sum1 + sum1
  sum2 = sum2 + sum2 !<<<<< 1,11, pass
  PRINT *, 'sum1=', sum1
  PRINT *, 'sum2=', sum2
END SUBROUTINE MySub
