program reindentIfConstruct
                                     if (1 .GE. 1) then
                                     	a = 1
                                     end if
    !<<<<< 2, 38, 4, 44, pass
    
    !!! This test shows the refactoring successfully converting a valid IF construct to a valid IF statement, and 
    !!! then reindenting the section of code to the correct location, based on code context.
end program