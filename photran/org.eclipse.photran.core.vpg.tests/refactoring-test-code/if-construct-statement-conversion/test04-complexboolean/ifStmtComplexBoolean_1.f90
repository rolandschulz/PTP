program ifStmtComplexBoolean_1
    implicit none
    integer :: x, y, a
    print *, "This is a test"
    if (x .LT. y .OR. y .GT. 5 .AND. 6 .GE. 6) a = 1 !<<<<< 5, 5, 5, 53, pass
    
    !!! This tests shows the refactoring successfully converting a valid IF statement to a valid IF construct, even
    !!! with a complex boolean guardian expression.
end program