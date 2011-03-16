! USER STORY 1, TEST 2
! Adds SAVE attribute to the initialized declaration statements for variables
! first_call_counter and second_call_counter

PROGRAM MyProgram !<<<<< 1, 1, pass
  CALL MySub
  CALL MySub
END PROGRAM MyProgram

SUBROUTINE MySub
  INTEGER :: first_call_counter = 0
  INTEGER :: second_call_counter = 10
  first_call_counter = first_call_counter + 1
  second_call_counter = second_call_counter + 1
  PRINT *, 'called:', first_call_counter
  PRINT *, 'called:', second_call_counter
END SUBROUTINE MySub
