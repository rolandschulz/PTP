! USER STORY 2, TEST 2
! Does not add SAVE attribute to the initialized declaration statement for
! variable j due to the presence of the unspecified SAVE statement in the same
! subroutine, but does add SAVE attribute to the initialized declaration
! statement for variable n with no SAVE statement in the same subroutine

PROGRAM MyProgram !<<<<< 1, 1, pass 
  CALL MySubOne
  CALL MySubOne
  CALL MySubTwo
  CALL MySubTwo
END PROGRAM MyProgram

SUBROUTINE MySubOne
  SAVE
  INTEGER :: i
  INTEGER :: j = 0
  i = i + 1
  j = j + 5
  PRINT *, 'i=', i, ', j=', j
END SUBROUTINE MySub

SUBROUTINE MySubTwo
  INTEGER :: m
  INTEGER :: n = 0
  m = m + 1
  n = n + 5
  PRINT *, 'm=', m, ', n=', n
END SUBROUTINE MySub
