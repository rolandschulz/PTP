! USER STORY 5, TEST 2
! Adds variable sum2 to a previously existing SAVE statement due to variable not
! having a declaration statement while being initialized by a DATA statement

! EXAMPLE FROM USER STORY

PROGRAM MyProgram !<<<<< 1,1, pass
  CALL MySub
  CALL MySub
END PROGRAM MyProgram

SUBROUTINE MySub
  DATA sum1, sum2 /100, 200/
  SAVE sum1
  sum1 = sum1 + sum1
  sum2 = sum2 + sum2
  PRINT *, 'sum1=', sum1
  PRINT *, 'sum2=', sum2
END SUBROUTINE MySub
