!         NOTE 4.42
!         An example of a type and a type-bound procedure is:

module mod
         TYPE POINT
           REAL :: X, Y
         CONTAINS
           PROCEDURE, PASS :: LENGTH => POINT_LENGTH
         END TYPE POINT

!         and in the module-subprogram-part of the same module:

         REAL FUNCTION POINT_LENGTH (A, B)
           CLASS (POINT), INTENT (IN) :: A, B
           POINT_LENGTH = SQRT ( (A%X - B%X)**2 + (A%Y - B%Y)**2 )
         END FUNCTION POINT_LENGTH
end module
