subroutine prob
        integer, parameter :: ROWS = 5, COLS = 7
        real :: matrix(ROWS, COLS)
        matrix(:, :) = 0.0          !<<<<<START !<<<<<END
        print *, matrix
end subroutine

program main; call prob; call flush; stop; end program
