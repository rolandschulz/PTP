program basictest
    call simple(4,3,2)
    
    call simple(4,Gamma=2,Beta=3)
end program basictest

subroutine simple(Alpha, Beta, Gamma) !<<<<< 7,1,7,5,2,1,0,pass
    integer, intent(in) :: Alpha
    integer, intent(out) :: Beta
    integer, intent(inout) :: Gamma
end subroutine