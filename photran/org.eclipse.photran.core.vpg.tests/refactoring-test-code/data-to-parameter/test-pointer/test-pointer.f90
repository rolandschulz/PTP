program testpointer !<<<<< 1,1,fail-final
    integer, pointer :: p
    integer :: q,x,y,z
    
    data x / 1 /
    
    data q,p,y / 3, null(), 1 /
    
    data z / 2 /

end program testpointer