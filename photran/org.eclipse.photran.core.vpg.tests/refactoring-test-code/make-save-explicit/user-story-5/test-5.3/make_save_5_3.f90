! USER STORY 5, TEST 3
! Adds SAVE statement for variable i due to variable not having declaration
! statement while being initialized by a DATA statement (variable unused,
! otherwise), but does not add SAVE statement for variable j due to variable
! having neither a declaration statement nor a DATA statement for its
! initialization

PROGRAM MyProgram !<<<<< 1, 1, pass
  CALL MySub
  CALL MySub
END PROGRAM MyProgram

SUBROUTINE MySub
  DATA i /0/
  j = 0
END SUBROUTINE MySub
