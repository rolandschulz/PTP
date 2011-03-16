program multipleIfSelections1
    if (x .LT. y .OR. y .GT. 5 .AND. 6 .GE. 6) a = 1
    if (.true.) then
	    a = 2
    end if 
    !<<<<< 2, 5, 5, 11, fail-initial
    
    !!! This test shows the refactoring failing the initial precondition because multiple IF statements and IF
    !!! constructs are selected, while the refactoring can only handle one at a time.
end program