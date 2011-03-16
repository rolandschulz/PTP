! USER STORY 2, TEST 3
! Adds SAVE attribute to the initialized declaration statement for variable j
! with a SAVE statement for a different variable in the same subroutine

PROGRAM MyProgram !<<<<< 7, 9, pass 
  CALL MySub
  CALL MySub
END PROGRAM MyProgram

SUBROUTINE MySub
  SAVE i
  INTEGER :: i
  INTEGER :: j = 0
  i = i + 1
  j = j + 5
  PRINT *, 'i=', i, ', j=', j
END SUBROUTINE MySub
