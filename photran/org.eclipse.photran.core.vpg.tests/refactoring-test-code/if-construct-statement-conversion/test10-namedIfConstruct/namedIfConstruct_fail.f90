program namedIfConstruct_fail
    myifconstruct: if (.true.) then
        a = 3
    end if myifconstruct
    print *, "This test tries to convert a named if construct to if statement" !<<<<< 2, 5, 4, 11, fail-initial
    
    !!! This test shows the refactoring failing the initial precondition because while the selected text is a
    !!! valid IF construct, it is a named IF construct, and therefore is not refactorable to an IF statement
end program