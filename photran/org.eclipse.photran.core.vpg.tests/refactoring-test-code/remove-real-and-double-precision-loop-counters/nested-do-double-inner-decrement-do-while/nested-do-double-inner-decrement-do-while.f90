! Check that nested DO loop doesn't affect refactoring
! behavior. Select inner DO loop - DOUBLE data type
! and decrement behavior - explicit step count.
! (This test selecting to replace with DO WHILE loop.)

PROGRAM NestedDoDoubleInnerDecrementDoWhile
  DOUBLE PRECISION :: counter, sum, counterin, sumin
  sum = 0.0
  sumin = 0.0
  DO counter = 1.2, 1.8, 0.1
    sum = sum + counter
    DO counterin = 1.8, 1.2, 0.1                    !<<<<< 12, 5, 12, 33, 1, pass
      sumin = sumin + counterin
    END DO
  END DO
  PRINT *, sum
END PROGRAM NestedDoDoubleInnerDecrementDoWhile
