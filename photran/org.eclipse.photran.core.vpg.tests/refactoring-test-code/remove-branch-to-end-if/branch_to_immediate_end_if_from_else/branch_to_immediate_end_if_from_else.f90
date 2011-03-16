! END IF selected that only has its own GOTO targetting
! it so no refactoring is performed by design and 
! user it told so.

PROGRAM branch_to_immediate_end_if_from_else
   INTEGER :: sum, i
   sum = 0
   DO 20, i = 1, 10
     IF (MOD(i,2).eq.0) THEN
       sum = sum + i
       IF (sum.ge.100) THEN
          GOTO 10
       ELSE
          sum = sum + sum
          GOTO 30
30     END IF	!<<<<< 16, 1, 16, 9, fail-initial
40     CONTINUE
10 END IF
20 CONTINUE
   PRINT *, 'sum:', sum
END PROGRAM branch_to_immediate_end_if_from_else
