! USER STORY 8, TEST 1
! Adds SAVE attribute to the initialized declaration statement for variable j
! in subroutine MySub, but not to the initialized declaration statement for
! variable i in program MyProgram

PROGRAM MyProgram !<<<<< 1, 1, pass
  INTEGER :: i = 0
  i = i + 1
  PRINT *, 'called:', i
  i = i + 1
  PRINT *, 'called:', i
  CALL MySub
  CALL MySub
END PROGRAM MyProgram

SUBROUTINE MySub
  INTEGER :: j = 10
  j = j + 1
  PRINT *, 'called:', j
END SUBROUTINE MySub
