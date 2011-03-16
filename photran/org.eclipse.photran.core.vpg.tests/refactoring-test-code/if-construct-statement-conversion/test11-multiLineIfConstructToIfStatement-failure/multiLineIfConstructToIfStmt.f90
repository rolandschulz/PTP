program convert_multiLineIfConstructToIfStatement
    if (.true.) then
        a = 2
        print *, 'hello'
        print *, 'world'
    end if
    print *, 3+4*5+6 !<<<<< 2, 5, 6, 11, fail-initial
    
    !!! This test shows the refactoring failing the initial precondition because while the selected text is a valid
    !!! IF construct, the body contains multiple lines of valid statements, and therefore, is not refactorable to
    !!! an IF statement.
end program