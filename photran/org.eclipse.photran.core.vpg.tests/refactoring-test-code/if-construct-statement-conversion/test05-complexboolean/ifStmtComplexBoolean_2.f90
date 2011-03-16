program ifStmtComplexBoolean_2
    implicit none
    integer :: x, y, z, a
    print *, "This is a test"
    if ((x*2+3)/z .GE. y) a = 1 !<<<<< 5, 5, 5, 32, pass
    
    !!! This tests shows the refactoring successfully converting a valid IF statement to a valid IF construct, even
    !!! with a complex boolean guardian expression.
end program