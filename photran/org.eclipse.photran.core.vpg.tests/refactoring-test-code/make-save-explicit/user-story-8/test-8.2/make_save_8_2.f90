! USER STORY 8, TEST 2
! Adds SAVE attribute to the declaration statement for variable j initialized
! through a DATA statement in subroutine MySub, but not to the declaration
! statement for variable i also initialized through a DATA statement in program
! MyProgram

PROGRAM MyProgram !<<<<< 1, 1, pass
  INTEGER :: i
  DATA i /0/
  i = i + 1
  PRINT *, 'called:', i
  i = i + 1
  PRINT *, 'called:', i
  CALL MySub
  CALL MySub
END PROGRAM MyProgram

SUBROUTINE MySub
  INTEGER :: j
  DATA j /0/
  j = j + 1
  PRINT *, 'called:', j
END SUBROUTINE MySub
