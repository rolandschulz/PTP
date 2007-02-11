!         NOTE 4.37
!         A pointer component of a derived type may be default-initialized to have an initial target.

               TYPE NODE
                 INTEGER              :: VALUE = 0
                 TYPE (NODE), POINTER :: NEXT_NODE => SENTINEL
               END TYPE

               TYPE(NODE), SAVE, TARGET :: SENTINEL
end
