! USER STORY 8, TEST 3
! Adds SAVE statement for variables j due to variable not having declaration
! statement while being initialized by a DATA statement in subroutine MySub,
! but doesn't add it for variables i due to variable not having declaration
! statement while being initialized by a DATA statement in program MyProgram

PROGRAM MyProgram !<<<<< 1, 1, pass
  DATA i /0/
  i = i + 1
  PRINT *, 'called:', i
  i = i + 1
  PRINT *, 'called:', i
  CALL MySub
  CALL MySub
END PROGRAM MyProgram

SUBROUTINE MySub
  DATA j /0/
  j = j + 1
  PRINT *, 'called:', j
END SUBROUTINE MySub
