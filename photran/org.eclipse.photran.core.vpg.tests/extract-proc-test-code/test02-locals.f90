call sub1()
contains

    subroutine sub1
        integer :: a, b
        dimension a(3, 4:5)
        print *, a                  !<<<<<START
        print *, b
        print *, implicit1
        print *, implicit2
        if (implicit2 .gt. 3) then
          print *, "OK"
        end if                      !<<<<<END
    end subroutine

    subroutine x
    end subroutine x
end program
