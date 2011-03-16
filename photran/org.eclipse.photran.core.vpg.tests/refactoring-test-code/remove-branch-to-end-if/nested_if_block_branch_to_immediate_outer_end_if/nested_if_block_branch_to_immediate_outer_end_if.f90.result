! Only GOTO targets the selected END IF for refactoring
! but since a refactoring would not make any changes
! by design we fail and warn the user that no change
! would occur. (Other GOTOs target other labels.)

PROGRAM nested_if_block_branch_to_immediate_inner_end_ifsubroutine
   INTEGER :: sum, i
   sum = 0
   DO 20, i = 1, 10
     IF (MOD(i,2).eq.0) THEN
       sum = sum + i
       IF (sum.ge.100) THEN
          GOTO 30
       ELSE
          sum = sum + sum
30     END IF
40     CONTINUE
       GOTO 10
10 END IF                           !<<<<< 19, 1, 19, 9, fail-initial
20 CONTINUE
   PRINT *, 'sum:', sum
END PROGRAM nested_if_block_branch_to_immediate_inner_end_ifsubroutine
