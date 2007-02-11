!           NOTE 4.60
!           For example, if the variable TEXT were declared (5.2) to be

           CHARACTER, DIMENSION (1:400), TARGET :: TEXT

!           and BIBLIO were declared using the derived-type definition REFERENCE in Note 4.35

           TYPE (REFERENCE) :: BIBLIO

!           the statement

           BIBLIO = REFERENCE (1, 1987, 1, "This is the title of the referenced &
                                           &paper", TEXT)
end

!           is valid and associates the pointer component SYNOPSIS of the object BIBLIO with the target
!           object TEXT.
