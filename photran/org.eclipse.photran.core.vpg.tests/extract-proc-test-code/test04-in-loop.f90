subroutine hmmm
    do 10 i = 1, 5
       print *, i                   !<<<<<START
       if (i .gt. 3) then
         print *, i * 10
       end if                       !<<<<<END
       print *, i
10  continue
end subroutine

program main; call hmmm; call flush; stop; end program
