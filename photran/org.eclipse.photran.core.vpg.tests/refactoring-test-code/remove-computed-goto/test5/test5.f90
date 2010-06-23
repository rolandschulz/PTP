program test5
    implicit none

    integer :: n

    n = 1

    ! this is a comment
    goto (123) n*2 !<<<<< 9, 5, 9, 19, pass
    ! another comment

    print *, "It wasn't handled"
    stop

123 print *, "It was one"
    stop

end program test5
