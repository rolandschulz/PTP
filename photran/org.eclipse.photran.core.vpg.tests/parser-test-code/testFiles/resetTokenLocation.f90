! hi
! bye
MODULE moduleZiggurat
   IMPLICIT NONE
   PUBLIC  :: uni
CONTAINS
FUNCTION uni( ) RESULT( fn_val )
   REAL(2)  ::  fn_val ;  INTEGER :: x

  WRITE(*,*) &!hi
   x

  WRITE(*,*) 'he &  

   &llasdasasd&
   &o'

  WRITE(*,*) 'hi   &
&bye'
    ! f
   fn_val = 0.5
   RETURN
END FUNCTION uni


SUBROUTINE zigset( jsrseed )

   INTEGER, INTENT(IN)  :: jsrseed
   RETURN

END SUBROUTINE zigset

END MODULE moduleZiggurat

!hello
PROGRAM hi
  USE moduleZiggurat
  
  IMPLICIT NONE

  INCLUDE 'includeZiggurat.fh'

  DO  i = 1, 10000

     x = uni( )
  END DO

	CALL zigset( &
	& i  &
	& )

END PROGRAM hi
