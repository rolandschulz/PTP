! Using a nested IF block check that GOTO of inner IF
! block is still considered an outer GOTO statement 
! so a CONTINUE statement is inserted and that GOTO
! (and other outer GOTOs) target the new CONTINUE statement

PROGRAM NestedIfBlockBasic
   INTEGER :: k, i
   READ(*,*) k
   IF (k.lt.10) THEN
     GOTO 20
   END IF
   i = k - 10
   IF (i.gt.100)  THEN
     i = i - 100
     IF (i.lt.10) THEN
       GOTO 20
     END IF
     i = i - 10
20 END IF                       !<<<<< 19, 1, 19, 10, pass
   PRINT *, i
END PROGRAM NestedIfBlockBasic
