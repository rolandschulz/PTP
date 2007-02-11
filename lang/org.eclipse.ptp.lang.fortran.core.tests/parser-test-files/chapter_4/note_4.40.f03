!          NOTE 4.40
!          An example of a type with private components is:

             MODULE DEFINITIONS
               TYPE POINT
                  PRIVATE
                  REAL :: X, Y
               END TYPE POINT
             END MODULE DEFINITIONS

!          Such a type definition is accessible in any scoping unit accessing the module via a USE state-
!          ment; however, the components X and Y are accessible only within the module, and within its
!          descendants.
