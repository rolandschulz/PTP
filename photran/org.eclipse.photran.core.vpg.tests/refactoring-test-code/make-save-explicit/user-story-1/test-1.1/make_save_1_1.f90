! USER STORY 1, TEST 1
! Adds SAVE attribute to the initialized declaration statement for variable
! call_counter

! EXAMPLE FROM USER STORY

PROGRAM MyProgram !<<<<< 1, 1, pass
  CALL MySub
  CALL MySub
END PROGRAM MyProgram

SUBROUTINE MySub
  INTEGER :: call_counter = 0
  call_counter = call_counter + 1
  PRINT *, 'called:', call_counter
END SUBROUTINE MySub
