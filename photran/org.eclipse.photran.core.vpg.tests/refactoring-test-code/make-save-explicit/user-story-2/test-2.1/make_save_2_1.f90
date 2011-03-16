! USER STORY 2, TEST 1
! Does not add SAVE attribute to the initialized declaration statement for
! variable j due to the presence of the unspecified SAVE statement in the same
! subroutine

! EXAMPLE FROM USER STORY

PROGRAM MyProgram !<<<<< 1, 1, pass 
  CALL MySub
  CALL MySub
END PROGRAM MyProgram

SUBROUTINE MySub
  SAVE
  INTEGER :: i
  INTEGER :: j = 0
  i = i + 1
  j = j + 5
  PRINT *, 'i=', i, ', j=', j
END SUBROUTINE MySub
