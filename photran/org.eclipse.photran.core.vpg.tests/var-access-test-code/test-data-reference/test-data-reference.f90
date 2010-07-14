program testdataref
    type myType
        integer :: var1
    end type
    
    type(myType) :: instance
    
    integer :: array(5)

    array(instance%var1) = 5
    !<<<<< 10,5,WRITE
    !<<<<< 10,11,READ

end program testdataref