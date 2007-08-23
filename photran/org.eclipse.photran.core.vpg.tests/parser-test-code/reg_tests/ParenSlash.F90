MODULE X
   ! (/) should be three tokens, not (/ followed by )
   INTERFACE OPERATOR (/)
      MODULE PROCEDURE BIG_DIV_INT, &
                       BIG_DIV_BIG
   END INTERFACE
END MODULE X
