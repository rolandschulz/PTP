MODULE pgmName
   IMPLICIT NONE


   PUBLIC  :: hi


CONTAINS

!  Generate uniformly distributed random numbers
FUNCTION hi( ) RESULT( fn_val )
   REAL(2)  ::  fn_val

   fn_val = 0.5
   RETURN
END FUNCTION hi

!SUBROUTINE hi
!	PRINT *,'Hello'
!END SUBROUTINE hi


END MODULE pgmName
