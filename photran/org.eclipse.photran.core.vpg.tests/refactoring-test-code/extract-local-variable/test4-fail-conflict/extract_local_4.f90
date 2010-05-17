program extract_local
    implicit none
    integer :: var ! Attempting to extract to var should fail since that variable already exists
    print *, "This is a test"
    print *, 3+4*5+6 !<<<<< 5, 16, 5, 19, integer :: var, false
end program
