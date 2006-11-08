!
! Strictly not a legal file as there can only be one program
!

program main
end program main

!
! These programs have unacticipated parse trees, i.e., second main_program has only an
! end_program_stmt, END PROGRAM, no program_stmt.
!

PROGRAM main
END

PROGRAM main
END PROGRAM

PROGRAM main
END 

ENDPROGRAM

END

