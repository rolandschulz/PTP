subroutine prob
        integer, parameter :: SIZE = 5
        real :: matrix(SIZE, SIZE)
        matrix(:, :) = 0.0          !<<<<<START !<<<<<END
        print *, matrix
end subroutine

program main; call prob; call flush; stop; end program
