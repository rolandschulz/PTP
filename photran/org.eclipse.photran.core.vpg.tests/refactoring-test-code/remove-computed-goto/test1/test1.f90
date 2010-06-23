program test1
    implicit none

    integer :: n

    n = 1

    goto (10, 20, 30, 40) n !<<<<< 8, 5, 8, 28, pass

    print *, "It wasn't handled"
    stop

10  print *, "It was one"
    stop

20  print *, "It was two"
    stop

30  print *, "It was three"
    stop

40  print *, "It was four"
    stop

end program test1
