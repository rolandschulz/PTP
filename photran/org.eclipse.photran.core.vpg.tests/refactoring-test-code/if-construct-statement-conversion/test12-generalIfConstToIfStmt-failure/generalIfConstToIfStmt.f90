program convert_blockIfConstructToIfStatement
    if (x .LT. 1) then
        a = 2
    else if (x .GE. 1) then
        a = 4
    else
        a = 6
    end if !<<<<< 2, 5, 8, 11, fail-initial
    
    !!! This test shows the refactoring failing the initial precondition because while the selected text is a valid
    !!! IF construct, the body contains multiple lines of valid statements, and therefore, is not refactorable to
    !!! an IF statement.
end program