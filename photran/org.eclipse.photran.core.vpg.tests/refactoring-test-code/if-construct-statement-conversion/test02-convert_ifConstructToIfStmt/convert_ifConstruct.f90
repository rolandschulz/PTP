program convert_ifConstructToIfStmt
    implicit none
    print *, "This is a test"
    if(.true.) then
      a = 1
    end if
    !<<<<< 4, 5, 6, 11, pass
    
    !!! This test shows the refactoring successfully refactoring a simple valid IF construct into a valid IF statement.
end program