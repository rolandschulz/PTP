program notAValidIf
    implicit none
    print *, "This is a test"
    print *, 3+4*5+6 !<<<<< 4, 5, 4, 22, fail-initial
    
    !!! This test shows the refactoring failing the initial precondition because the selected text is a print statement, and
    !!! not a valid IF statement or construct
end program