program testarray !<<<<< 1,1,fail-final
    integer :: myArray(5)
    integer :: a,x,y,z
    integer :: matrix(5,5)
    
    data x / 2 /
    
    data y,myArray,z /2, 3,4,5,6,7, 4/
    
    data ((matrix(i,j), i=1,5,2), j=1,5) / 15*0 /
    
    data a / 3 /

end program testarray