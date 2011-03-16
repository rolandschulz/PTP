! Check that nested DO loop doesn't affect refactoring
! behavior. Select outer DO loop - REAL data type
! and decrement behavior - explicit step count.

PROGRAM NestedDoRealOuterIncrement
  REAL :: counter, sum, counterin, sumin
  sum = 0.0
  sumin = 0.0
  DO counter = 1.2, 1.8, 0.1                    !<<<<< 9, 3, 9, 29, 0, pass
    sum = sum + counter
    DO counterin = 1.2, 1.8, 0.1
      sumin = sumin + counterin
    END DO
  END DO
  PRINT *, sum
END PROGRAM NestedDoRalOuterIncrement
