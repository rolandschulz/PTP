program test2
    implicit none

    integer :: n

    n = 1

    goto (10, 20, 30) n*2 !<<<<< 8, 5, 8, 30, pass

    print *, "It wasn't handled"
    stop

10  print *, "It was one"
    stop

20  print *, "It was two"
    stop

30  print *, "It was three"
    stop

end program test2
