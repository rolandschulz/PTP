! USER STORY 4, TEST 1
! Adds SAVE attribute to the declaration statements for variables sumOne and
! sumTwo initialized through the same DATA statement

PROGRAM MyProgram !<<<<< 1, 1, pass 
  CALL MySub
  CALL MySub
END PROGRAM MyProgram

SUBROUTINE MySub
  INTEGER :: sumOne
  INTEGER :: sumTwo
  DATA sumOne /100/ sumTwo /10/
  sumOne = sumOne + sumOne
  sumTwo = sumTwo + sumTwo
  PRINT *, 'sumOne=', sumOne, 'sumTwo=', sumTwo
END SUBROUTINE MySub
