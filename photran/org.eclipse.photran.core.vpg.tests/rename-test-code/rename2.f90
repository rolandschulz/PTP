! Header comment
program Main ! After program name
    implicit none
    integer :: one ! After declaration
    integer, parameter :: two = 2 ! After declaration assignment
    integer :: three
    
    ! Between specifications
    
    integer(two) :: four

    print *, one !
    print *, two, &
        three
    print *, four &
        , one
    print *, two, & !
        three !
    print *, four & !
        , one !

    print *, "This is a really&
            & long string"

    stop
! end
end program Main !End
! end of file
! end of file
