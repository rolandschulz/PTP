! USER STORY 3, TEST 2
! Adds SAVE attribute to the declaration statement for array myArray
! initialized through a DATA statement

PROGRAM MyProgram !<<<<< 8,16, pass
  CALL MySub
  CALL MySub
END PROGRAM MyProgram

SUBROUTINE MySub
  INTEGER, DIMENSION(2) :: myArray
  DATA myArray /100, 10/
  myArray(1) = myArray(1) + 1
  myArray(2) = myArray(2) + 1
  PRINT *, 'myArray(1)=', myArray(1), 'myArray(2)=', myArray(2)
END SUBROUTINE MySub
