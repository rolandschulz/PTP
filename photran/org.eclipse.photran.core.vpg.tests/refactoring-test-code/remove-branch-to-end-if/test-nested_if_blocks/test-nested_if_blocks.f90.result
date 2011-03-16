! Even with more complicated nested IF and DO structure
! since any GOTO inside any statements of the selected
! END IF line are considered inner IFs then it should
! be retargetted to the following CONTINIUE statement
! and the original END IF label removed.

PROGRAM test_nested_if_blocks
   INTEGER :: sum, i
   sum = 0
   DO 20, i = 1, 10
     IF (MOD(i,2).eq.0) THEN
       sum = sum + i
       IF (sum.ge.100) THEN
          GOTO 10
       ELSE
          sum = sum + sum
       END IF
10 END IF                           !<<<<< 18, 1, 18, 10, fail-initial
20 CONTINUE
   PRINT *, 'sum:', sum
END PROGRAM test_nested_if_blocks
