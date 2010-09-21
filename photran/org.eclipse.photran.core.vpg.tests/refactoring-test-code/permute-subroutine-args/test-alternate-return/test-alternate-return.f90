program testaltreturn
    call testsub(4,3,200,10,200,E=10,D=2)
    
200 print *, "hello, world!"

end program

subroutine testsub(A,B,*,C,*,D,E) !<<<<< 8,1,8,5,2,1,0,4,3,6,5,pass

end subroutine