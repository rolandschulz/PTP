subroutine s
end subroutine  
                

program bug212606
    implicit none

    type OutputDataDescript_t
        integer n
    end type

    type (OutputDataDescript_t) :: descript

    CHARACTER(LEN=4) :: type1
    CHARACTER(LEN=4) :: type2

    integer :: ns,type

    type1 = type2

    type = 2

end program
