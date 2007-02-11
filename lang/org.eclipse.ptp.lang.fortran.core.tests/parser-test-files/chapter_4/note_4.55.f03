!          NOTE 4.60
!          The following is an example of procedure overriding, expanding on the example in Note 4.47.

module mod
          TYPE, EXTENDS (POINT) :: POINT_3D
            REAL :: Z
          CONTAINS
            PROCEDURE, PASS :: LENGTH => POINT_3D_LENGTH
          END TYPE POINT_3D

!          and in the module-subprogram-part of the same module:

contains

          REAL FUNCTION POINT_3D_LENGTH ( A, B )
            CLASS (POINT_3D), INTENT (IN) :: A
            CLASS (POINT), INTENT (IN) :: B
            SELECT TYPE(B)
              CLASS IS(POINT_3D)
                POINT_3D_LENGTH = SQRT( (A%X-B%X)**2 + (A%Y-B%Y)**2 + (A%Z-B%Z)**2 )
                RETURN
            END SELECT
            PRINT *, 'In POINT_3D_LENGTH, dynamic type of argument is incorrect.'
            STOP
          END FUNCTION POINT_3D_LENGTH
end module