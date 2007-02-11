!        NOTE 4.29
!        An example of a derived type definition with an allocatable component is:

        TYPE STACK
           INTEGER              :: INDEX
           INTEGER, ALLOCATABLE :: CONTENTS (:)
        END TYPE STACK
end

!        For each scalar variable of type STACK, the shape of the component CONTENTS is determined
!        by execution of an ALLOCATE statement or assignment statement, or by argument association.
