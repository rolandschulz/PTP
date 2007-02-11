!        NOTE 4.31
!        An example of a derived type definition with a pointer component is:

        TYPE REFERENCE
           INTEGER                                         ::   VOLUME, YEAR, PAGE
           CHARACTER (LEN = 50)                            ::   TITLE
           PROCEDURE (printer_interface), POINTER          ::   PRINT => NULL()
           CHARACTER, DIMENSION (:), POINTER               ::   SYNOPSIS
        END TYPE REFERENCE
end

!        Any object of type REFERENCE will have the four nonpointer components VOLUME, YEAR,
!        PAGE, and TITLE, the procedure pointer PRINT, which has an explicit interface the same as
!        printer interface, plus a pointer to an array of characters holding SYNOPSIS. The size of this
!        target array will be determined by the length of the abstract. The space for the target may be
!        allocated (6.3.1) or the pointer component may be associated with a target by a pointer assignment
!        statement (7.4.2).
