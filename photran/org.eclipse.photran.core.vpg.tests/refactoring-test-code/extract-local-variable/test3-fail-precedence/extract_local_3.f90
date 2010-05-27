program extract_local
    implicit none
    print *, "This is a test"
    ! Attempting to extract 5+6 should fail
    print *, 3+4*5+6 !<<<<< 5, 18, 5, 21, integer :: var, fail-initial
end program
