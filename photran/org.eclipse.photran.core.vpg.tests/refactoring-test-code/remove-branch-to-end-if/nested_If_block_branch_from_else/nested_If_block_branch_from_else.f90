! With a DO statement enclosing nested IF blocks make sure
! existing CONTINUE statement is targeted for outer GOTOs
! and since no inner GOTO exists remove the label of 
! selected END IF (check reindentation as well).

PROGRAM nested_if_block_branch_from_else
   INTEGER :: sum, i
   sum = 0
   DO 20, i = 1, 10
     IF (MOD(i,2).eq.0) THEN
       sum = sum + i
       IF (sum.ge.100) THEN
          GOTO 30
       ELSE
          sum = sum + sum
          GOTO 10
30     END IF
40     CONTINUE
10 END IF                           !<<<<< 19, 1, 19, 9, fail-initial
20 CONTINUE
   PRINT *, 'sum:', sum
END PROGRAM nested_if_block_branch_from_else
