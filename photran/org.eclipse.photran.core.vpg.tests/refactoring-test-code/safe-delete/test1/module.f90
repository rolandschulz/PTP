module module  !TODO 1,8,false
    implicit none
contains
    subroutine s1; end subroutine !<<<<<4,16,true, (never invoked)
    subroutine s2; end subroutine !<<<<<5,16,false
    subroutine s3; end subroutine !<<<<<6,16,false
end module module
