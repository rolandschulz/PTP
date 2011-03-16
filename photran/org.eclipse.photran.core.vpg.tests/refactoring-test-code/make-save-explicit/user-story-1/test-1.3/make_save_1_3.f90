! USER STORY 1, TEST 3
! Adds SAVE attribute to the initialized declaration statement for variable
! first_call_counter, but not to the non-initialized declaration statement for
! variable second_call_counter

! EXAMPLE FROM USER STORY

PROGRAM MyProgram !<<<<< 1, 1, pass
  CALL MySub
  CALL MySub
END PROGRAM MyProgram

SUBROUTINE MySub
  INTEGER :: first_call_counter = 0
  INTEGER :: second_call_counter
  second_call_counter = 10
  first_call_counter = first_call_counter + 1
  second_call_counter = second_call_counter + 1
  PRINT *, 'called:', first_call_counter
  PRINT *, 'called:', second_call_counter
END SUBROUTINE MySub
