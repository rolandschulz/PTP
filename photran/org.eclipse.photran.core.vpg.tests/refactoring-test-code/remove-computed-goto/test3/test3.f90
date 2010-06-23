program test3
    implicit none

    integer :: n

    n = 1

    ! this is a comment
    goto (10, 20, 30) n*2 !<<<<< 9, 5, 9, 26, pass
    ! another comment

    print *, "It wasn't handled"
    stop

10  print *, "It was one"
    stop

20  print *, "It was two"
    stop

30  print *, "It was three"
    stop

end program test3
