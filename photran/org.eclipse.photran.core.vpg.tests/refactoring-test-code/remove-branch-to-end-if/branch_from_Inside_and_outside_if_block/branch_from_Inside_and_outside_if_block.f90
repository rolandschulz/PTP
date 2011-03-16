! More complicated PROGRAM to check that outer GOTOs
! are retargetted to existing CONTINUE statement
! but END IF label is not removed since inner GOTO
! targets it.

PROGRAM branch_from_inside_and_outside_if_block
   INTEGER :: sum, i
   sum = 0
   DO 20, i = 1, 10
     IF (MOD(i,2).eq.0) THEN
       sum = sum + i
       IF (sum.ge.100) THEN
          GOTO 30
       ELSE
          sum = sum + sum
          GOTO 50
30     END IF
40     CONTINUE
10 END IF
20 CONTINUE
   PRINT *, 'sum:', sum
   IF (sum.ge.100) THEN
       PRINT *, 'sum:', sum
    ELSE
       sum = sum + sum
       GOTO 50
50 END IF	!<<<<< 27, 1, 27, 9, pass
60 CONTINUE
END PROGRAM branch_from_inside_and_outside_if_block
