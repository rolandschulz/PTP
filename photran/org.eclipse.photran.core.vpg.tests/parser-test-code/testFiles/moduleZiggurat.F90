MODULE moduleZiggurat
   IMPLICIT NONE


   PUBLIC  :: uni


CONTAINS

!  Generate uniformly distributed random numbers
FUNCTION uni( ) RESULT( fn_val )
   REAL(2)  ::  fn_val

   fn_val = 0.5
   RETURN
END FUNCTION uni



END MODULE moduleZiggurat

