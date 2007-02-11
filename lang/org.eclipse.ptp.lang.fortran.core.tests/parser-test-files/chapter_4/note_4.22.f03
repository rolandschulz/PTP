!         NOTE 4.22
!         An example of data entities in different scoping units having the same type is:

         PROGRAM PGM
            TYPE EMPLOYEE
               SEQUENCE
               INTEGER        ID_NUMBER
               CHARACTER (50) NAME
            END TYPE EMPLOYEE

            TYPE (EMPLOYEE) PROGRAMMER
            CALL SUB (PROGRAMMER)
         END PROGRAM PGM

         SUBROUTINE SUB (POSITION)
            TYPE EMPLOYEE
               SEQUENCE
               INTEGER        ID_NUMBER
               CHARACTER (50) NAME
            END TYPE EMPLOYEE

            TYPE (EMPLOYEE) POSITION
         END SUBROUTINE SUB
end
