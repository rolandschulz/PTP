! Check that nested DO loop doesn't affect refactoring
! behavior. Select inner DO loop - DOUBLE data type
! and decrement behavior - implicit step count.

PROGRAM NestedDoDoubleInnerDecrementImplicit
  DOUBLE PRECISION :: counter, sum, counterin, sumin
  sum = 0.0
  sumin = 0.0
  DO counter = 1.2, 1.8, 0.1
    sum = sum + counter
    DO counterin = 1.8, 1.2                         !<<<<< 11, 5, 11, 33, 0, pass
      sumin = sumin + counterin
    END DO
  END DO
  PRINT *, sum
END PROGRAM NestedDoDoubleInnerDecrementImplicit
