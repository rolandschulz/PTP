! When upper and lower bounds are the same value
! refactoring should proceed with same value used.

PROGRAM EqualLbUb
  REAL :: counter, sum
  sum = 0.0
  DO counter = 1.2, 1.2, 0.1        !<<<<< 7, 3, 7, 29, 0, pass
    sum = sum + counter
  END DO
  PRINT *, sum
END PROGRAM EqualLbUb
