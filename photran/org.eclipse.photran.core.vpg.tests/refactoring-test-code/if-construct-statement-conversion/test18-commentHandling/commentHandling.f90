program commentHandling
    implicit none
    integer :: x, y, a
    if (x .LT. y .OR. y .GT. 5 .AND. 6 .GE. 6) then
        a = 1 !This is an if statement
        !can add more statements here
        !more comments here
    end if
    print *, "This is a test" !<<<<< 4, 5, 8, 11, pass
    
    !!! This test shows the refactoring successfully converting a valid IF construct to a valid IF statement. Even though the IF
    !!! construct has multiple lines of code, only one of which is a valid statement, so it is therefore still refactorable. The
    !!! refactoring will then take the lines of comments and append them to the end of the IF statement in order to preserve them.
    !!! The user can then reformat the comments as they feel fit.
end program