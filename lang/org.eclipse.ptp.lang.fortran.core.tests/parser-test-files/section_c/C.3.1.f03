!     C.3.1    Structure components (6.1.2)
!
!     Components of a structure are referenced by writing the components of successive levels of the structure
!     hierarchy until the desired component is described. For example,

     TYPE ID_NUMBERS
        INTEGER SSN
        INTEGER EMPLOYEE_NUMBER
     END TYPE ID_NUMBERS

     TYPE PERSON_ID
        CHARACTER (LEN=30) LAST_NAME
        CHARACTER (LEN=1) MIDDLE_INITIAL
        CHARACTER (LEN=30) FIRST_NAME
        TYPE (ID_NUMBERS) NUMBER
     END TYPE PERSON_ID

     TYPE PERSON
        INTEGER AGE
        TYPE (PERSON_ID) ID
     END TYPE PERSON

     TYPE (PERSON) GEORGE, MARY

     PRINT *, GEORGE % AGE             ! Print the AGE component
     PRINT *, MARY % ID % LAST_NAME    ! Print LAST_NAME of MARY
     PRINT *, MARY % ID % NUMBER % SSN ! Print SSN of MARY
     PRINT *, GEORGE % ID % NUMBER ! Print SSN and EMPLOYEE_NUMBER of GEORGE

END

!     A structure component may be a data object of intrinsic type as in the case of GEORGE % AGE or it
!     may be of derived type as in the case of GEORGE % ID % NUMBER. The resultant component may
!     be a scalar or an array of intrinsic or derived type.

subroutine inserted_sub1

     TYPE LARGE
        INTEGER ELT (10)
        INTEGER VAL
     END TYPE LARGE

     TYPE (LARGE) A (5)       !     5 element array, each of whose elements
                              !     includes a 10 element array ELT and
                              !     a scalar VAL.
     PRINT *, A (1)           !     Prints 10 element array ELT and scalar VAL.
     PRINT *, A (1) % ELT (3) !     Prints scalar element 3
                              !     of array element 1 of A.
     PRINT *, A (2:4) % VAL   !     Prints scalar VAL for array elements
                              !     2 to 4 of A.
END

!     Components of an object of extensible type that are inherited from the parent type may be accessed as
!     a whole by using the parent component name, or individually, either with or without qualifying them
!     by the parent component name.

!     For example:

subroutine inserted_sub2

       TYPE POINT            ! A base type
         REAL :: X, Y
       END TYPE POINT

       TYPE, EXTENDS(POINT) :: COLOR_POINT ! An extension of TYPE(POINT)
         ! Components X and Y, and component name POINT, inherited from parent
         INTEGER :: COLOR
       END TYPE COLOR_POINT

       TYPE(POINT) :: PV = POINT(1.0, 2.0)
       TYPE(COLOR_POINT) :: CPV = COLOR_POINT(POINT=PV, COLOR=3)
       PRINT *, CPV%POINT                         ! Prints 1.0 and 2.0
       PRINT *, CPV%POINT%X, CPV%POINT%Y          ! And this does, too
       PRINT *, CPV%X, CPV%Y                      ! And this does, too

END