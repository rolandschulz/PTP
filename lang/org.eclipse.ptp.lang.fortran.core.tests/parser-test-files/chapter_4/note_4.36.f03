!         NOTE 4.36
!         A pointer component of a derived type may have as its target an object of that derived type. The
!         type definition may specify that in objects declared to be of this type, such a pointer is default
!         initialized to disassociated. For example:

         TYPE NODE
            INTEGER              :: VALUE = 0
            TYPE (NODE), POINTER :: NEXT_NODE => NULL ( )
         END TYPE
end
