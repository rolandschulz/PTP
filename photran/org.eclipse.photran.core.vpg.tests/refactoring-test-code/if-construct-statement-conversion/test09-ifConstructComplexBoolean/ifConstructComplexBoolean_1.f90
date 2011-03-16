program ifConstructComplexBoolean_1
    implicit none
    integer :: x, y, a
    print *, "This is a test"
    if (x .LT. y .OR. y .GT. 5 .AND. 6 .GE. 6) then
        a = 1
    end if
    !<<<<< 5, 5, 7, 11, pass
    
    !!! This tests shows the refactoring successfully converting a valid IF construct to a valid IF statement, even
    !!! with a complex boolean guardian expression.
end program