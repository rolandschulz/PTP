!         NOTE 4.21
!         An example of declaring two entities with reference to the same derived-type definition is:

         TYPE POINT
            REAL X, Y
         END TYPE POINT

         TYPE (POINT) :: X1
         CALL SUB (X1)

         CONTAINS

            SUBROUTINE SUB (A)
               TYPE (POINT) :: A
            END SUBROUTINE SUB
end
