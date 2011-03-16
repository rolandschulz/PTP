! USER STORY 1, TEST 4
! Adds SAVE attribute to the initialized declaration statement for array
! myArray

PROGRAM MyProgram !<<<<< 11, 18, pass
  CALL MySub
  CALL MySub
END PROGRAM MyProgram

SUBROUTINE MySub
  INTEGER, DIMENSION(3) :: myArray = (/ 1, 2, 3 /)
  myArray(1) = myArray(1) + 1
  myArray(2) = myArray(2) + 2
  myArray(3) = myArray(3) + 3
  PRINT *, 'myArray:', myArray(1), myArray(2), myArray(3)
END SUBROUTINE MySub
