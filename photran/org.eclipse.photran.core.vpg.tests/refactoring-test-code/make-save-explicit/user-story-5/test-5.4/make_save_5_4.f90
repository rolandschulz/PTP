! USER STORY 5, TEST 4
! Does not add variable i to existing SAVE statement due to variable names
! being case-insensitive, meaning that variable i is already explicitly saved
! due to the existing SAVE statement


PROGRAM MyProgram !<<<<< 1, 1, pass
  CALL MySub
  CALL MySub
END PROGRAM MyProgram

SUBROUTINE MySub
  SAVE I
  DATA i /0/
END SUBROUTINE MySub
