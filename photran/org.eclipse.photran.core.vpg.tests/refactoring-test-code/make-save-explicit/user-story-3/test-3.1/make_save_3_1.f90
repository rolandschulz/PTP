! USER STORY 3, TEST 1
! Adds SAVE attribute to the declaration statement for variable sum initialized
! through a DATA statement

! EXAMPLE FROM USER STORY

PROGRAM MyProgram !<<<<< 1, 1, pass 
  CALL MySub
  CALL MySub
END PROGRAM MyProgram

SUBROUTINE MySub
  INTEGER :: sum
  DATA sum /100/
  sum = sum + sum
  PRINT *, 'sum=', sum
END SUBROUTINE MySub
