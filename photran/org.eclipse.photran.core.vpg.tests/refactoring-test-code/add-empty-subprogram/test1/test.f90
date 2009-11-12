subroutine qwer
end subroutine qwer

program test  !<<<<<4,9,qqqqq,true, !<<<<<4,9,asdf,false, !<<<<<4,9,ghjk,true, !<<<<<4,9,qwer,false,
!TODO !4,9,s1,true, !4,9,s2,false, !4,9,s3,false,
    use module
    implicit none
    call asdf
    call qwer
    ! Don't call ghjk; it's OK if that is introduced and it shadows the existing external subroutine
    call s2
    call s3
end program test

subroutine asdf  !14,12,qqqqq,true, !14,12,s2,true, !14,12,s3,false,
    use module
    call s3
end subroutine asdf

subroutine ghjk
end subroutine ghjk
