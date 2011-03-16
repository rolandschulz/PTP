program convert_ifStatementToIfConstruct_incorrectSelection
    implicit none
    print *, "This is a test"
    integer :: if
    if = 5 !<<<<< 5, 5, 5, 11, fail-initial
    
    !!! This test shows the refactoring failing the initial precondition because the selected text is not a valid
    !!! IF statement or construct, even though the first token is "if"
end program