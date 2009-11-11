subroutine sub
    print *, "Hello" !<<<<<START !<<<<<END
end subroutine

program main; call sub; call flush; stop; end program
