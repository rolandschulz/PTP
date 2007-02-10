           MODULE EXAMPLE_MODULE
              IMPLICIT NONE

              INTERFACE
                 FUNCTION FUN (I)    ! Not all data entities need to
                    INTEGER FUN      ! be declared explicitly
                 END FUNCTION FUN
              END INTERFACE

           CONTAINS

              FUNCTION JFUN (J)      ! All data entities need to
                 INTEGER JFUN, J     ! be declared explicitly.

              END FUNCTION JFUN

           END MODULE EXAMPLE_MODULE

           SUBROUTINE SUB
              IMPLICIT COMPLEX (C)
              C = (3.0, 2.0)      ! C is implicitly declared COMPLEX

           CONTAINS

              SUBROUTINE SUB1
                 IMPLICIT INTEGER (A, C)
                 C = (0.0, 0.0) ! C is host associated and of
                                ! type complex
                 Z = 1.0        ! Z is implicitly declared REAL
                 A=2            ! A is implicitly declared INTEGER
                 CC = 1         ! CC is implicitly declared INTEGER

              END SUBROUTINE SUB1

              SUBROUTINE SUB2
                 Z = 2.0         ! Z is implicitly declared REAL and
                                 ! is different from the variable of
                                 ! the       same name in SUB1

              END SUBROUTINE SUB2

              SUBROUTINE SUB3
                 USE EXAMPLE_MODULE !        Accesses integer function FUN
                                    !       by use association
                 Q = FUN (K)        !       Q is implicitly declared REAL and
                                    !       K is implicitly declared INTEGER
              END SUBROUTINE SUB3

           END SUBROUTINE SUB
