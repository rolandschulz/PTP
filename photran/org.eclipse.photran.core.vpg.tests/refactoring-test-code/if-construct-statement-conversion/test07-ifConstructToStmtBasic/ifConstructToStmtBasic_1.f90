program ifConstructToStmtBasic_1
    if (.true.) then
	    a = 2
    end if
    !<<<<< 2, 5, 4, 11, pass
    
    !!! This tests shows the refactoring successfully converting a valid IF construct to a valid IF statement.
end program