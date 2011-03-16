! USER STORY 4, TEST 2
! Adds SAVE attribute to the declaration statements for arrays myArray and
! myArray2 initialized through the same DATA statement

PROGRAM MyProgram !<<<<< 8,16, pass
  CALL MySub
  CALL MySub
END PROGRAM MyProgram

SUBROUTINE MySub
  INTEGER, DIMENSION(2) :: myArray
  INTEGER, DIMENSION(2) :: myArray2
  DATA myArray /10, 20/  myArray2 /30, 40/

  myArray(1) = myArray(1) + 1
  myArray(2) = myArray(2) + 1

  myArray2(1) = myArray2(1) + 1
  myArray2(2) = myArray2(2) + 1

  PRINT *, 'myArray(1)=', myArray(1), 'myArray(2)=', myArray(2)
  PRINT *, 'myArray2(1)=', myArray2(1), 'myArray2(2)=', myArray2(2)
END SUBROUTINE MySub
