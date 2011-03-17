program test_case

    use somemodule

    implicit none

    MAIN_LOOP: do
        select case (mode)
            case (SOMETHING)
                if (.NOT. object%method(DO_IT = .TRUE.)) goto 300
            case default
                exit MAIN_LOOP
        end select
    enddo MAIN_LOOP

    stop

300 stop 'Failure'

end program test_case
