! USER STORY 7, TEST 1
! Adds SAVE statement for variable call_counter and call_counter2  
! due to variables being implicitly saved 

PROGRAM MyProgram !<<<<< 1, 1, pass
  CALL MySub
  CALL MySub
END PROGRAM MyProgram

SUBROUTINE MySub
  INTEGER call_counter = 10, call_counter2 = 20
  call_counter = call_counter + 1
  call_counter2 = call_counter2 + 2
  PRINT *, 'called:', call_counter, call_counter2
END SUBROUTINE MySub
