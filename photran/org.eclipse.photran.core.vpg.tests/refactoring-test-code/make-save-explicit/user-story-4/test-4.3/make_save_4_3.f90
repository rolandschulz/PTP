! USER STORY 4, TEST 3
! Adds SAVE attribute to the declaration statements for variables i, j, and k
! initialized through the same DATA statement (alternate arrangement of DATA
! statement)

PROGRAM MyProgram !<<<<< 8,16, pass
  CALL MySub
  CALL MySub
END PROGRAM MyProgram

SUBROUTINE MySub
  INTEGER :: i
  INTEGER :: j
  INTEGER :: k
  DATA i,j,k /10, 20, 35/

  i = i + 1
  j = j + 1
  k = k + 1

  PRINT *, 'i=', i, 'j=', j, 'k=', k
END SUBROUTINE MySub
