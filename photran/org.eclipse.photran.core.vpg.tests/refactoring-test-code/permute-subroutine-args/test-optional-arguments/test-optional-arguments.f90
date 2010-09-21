program testoptional
    call testsub(1,2,200,200,3,4,5,6)
    
    call testsub(1,2,200,200,F=3,D=4)
    
    call testsub(1,2,200,200,E=5,F=3,D=4)

200 print *, "hello, world!"
end program testoptional

subroutine testsub(A,B,*,*,C,D,E,F) !<<<<< 11,1,11,5,2,1,3,0,5,4,6,7,pass
    integer, optional :: C
    integer, optional :: E
end subroutine