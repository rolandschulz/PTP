subroutine qwer !<<<<<1,12,false
end subroutine qwer

program test  !TODO 4,9,true, (OK to delete main program)--but wont compile
    use module
    implicit none
    call asdf
    call qwer
    call s2
    call s3
end program test

subroutine asdf  !<<<<<13,12,false
    use module
    call s3
end subroutine asdf

subroutine ghjk !<<<<<18,12,true
end subroutine ghjk
