! With two CONTINUE statements (unique labels) the
! outter GOTO statement should be refactored to target
! the CONTINUE statement immediately following the
! selected END IF statement.

PROGRAM test_endif_duplicate_continue
   INTEGER :: sum, i
   sum = 0
   DO 20, i = 1, 10
     IF (MOD(i,2).eq.0) THEN
       GOTO 10
     END IF
     sum = sum + i
     IF (sum.ge.100) THEN
       sum = sum + sum
10   END IF                         !<<<<< 16, 1, 16, 12, pass
20 CONTINUE
30 CONTINUE
40 PRINT *, 'sum:', sum
END PROGRAM test_endif_duplicate_continue
