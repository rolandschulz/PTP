! USER STORY 5, TEST 1
! Adds SAVE statement for variables sum1 and sum2 due to variables not 
! having declaration statements while being initialized by a DATA statement

! EXAMPLE FROM USER STORY

PROGRAM MyProgram
  CALL MySub
  CALL MySub
END PROGRAM MyProgram

SUBROUTINE MySub
  DATA sum1, sum2 /100, 200/
  sum1 = sum1 + sum1
  sum2 = sum2 + sum2
  PRINT *, 'sum1=', sum1
  PRINT *, 'sum2=', sum2
END SUBROUTINE MySub
!<<<<< 1,1, pass
