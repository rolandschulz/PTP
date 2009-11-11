call sub1(); call flush; stop
contains

    subroutine sub1
        integer :: a, b
        dimension a(3, 4:5)
        do b = 1, 3; a(b, :) = b*10; enddo; implicit1 = 20; implicit2 = 30
        print *, a
        print *, b                  !<<<<<START !<<<<<END
        print *, implicit1
        print *, implicit2
        if (implicit2 .gt. 3) then
          print *, "OK"
        end if
    end subroutine

    subroutine x
    end subroutine x
end program
