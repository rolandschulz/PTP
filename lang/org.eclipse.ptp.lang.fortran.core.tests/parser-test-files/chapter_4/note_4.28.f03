!          NOTE 4.29
!          An example of a derived type definition with an array component is:

          TYPE LINE
             REAL, DIMENSION (2, 2) :: COORD               !
                                                           !   COORD(:,1) has the value of (/X1, Y1/)
                                                           !   COORD(:,2) has the value of (/X2, Y2/)
             REAL                          :: WIDTH        !   Line width in centimeters
             INTEGER                       :: PATTERN      !   1 for solid, 2 for dash, 3 for dot
          END TYPE LINE

!          An example of declaring a variable LINE SEGMENT to be of the type LINE is:

          TYPE (LINE)              :: LINE_SEGMENT
end
