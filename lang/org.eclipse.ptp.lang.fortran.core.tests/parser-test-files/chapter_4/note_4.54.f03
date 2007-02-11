!         NOTE 4.54
!         Examples:

module mod
         TYPE POINT                                     ! A base type
           REAL :: X, Y
         END TYPE POINT

         TYPE, EXTENDS(POINT) :: COLOR_POINT   ! An extension of TYPE(POINT)
           ! Components X and Y, and component name POINT, inherited from parent
           INTEGER :: COLOR
         END TYPE COLOR_POINT
end module