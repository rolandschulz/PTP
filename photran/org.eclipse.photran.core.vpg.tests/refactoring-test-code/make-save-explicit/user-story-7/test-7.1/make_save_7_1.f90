! USER STORY 7, TEST 1
! Adds SAVE statement for variable call_counter due to variable being
! initialized in its declaration statement without :: operator

PROGRAM MyProgram !<<<<< 1, 1, pass
  CALL MySub
  CALL MySub
END PROGRAM MyProgram

SUBROUTINE MySub
  INTEGER call_counter = 0
  call_counter = call_counter + 1
  PRINT *, 'called:', call_counter
END SUBROUTINE MySub
