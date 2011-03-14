program comment_surrounding_goto !<<<<<START
    implicit none
    integer anotherlabel

    ! before comment
    assign 10000 to anotherlabel ! line comment
    ! after comment

    ! before comment
10000    goto anotherlabel ! line comment
    ! after comment

end program comment_surrounding_goto !<<<<<END
