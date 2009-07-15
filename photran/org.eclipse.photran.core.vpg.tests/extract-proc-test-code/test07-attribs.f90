subroutine prob(m, n)
        implicit none
        integer, intent(in) :: m
        integer, intent(out) :: n
        integer, save :: saved = 10
        target :: saved
        integer, pointer :: p_saved
        integer, parameter :: ROWS = 5, COLS = 7
        real :: matrix(ROWS, COLS)
        p_saved => saved
        matrix(:, :) = 0.0           !<<<<<START
        n = 5 * m
        saved = 2
        !p_saved => saved
        !p_saved = p_saved + 1
        print *, n, m, saved !<<<<<END
        print *, n, m, saved, p_saved
end subroutine

program main; call prob(3, n); call prob(3, n); print *, n; call flush; stop; end program
