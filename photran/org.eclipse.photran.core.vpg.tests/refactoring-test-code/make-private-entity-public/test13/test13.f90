program private
    implicit none
    real, private :: blah
    integer, private, optional :: priv, blah
    real :: hi
    private :: hi

    intrinsic hello
    external goodbye
    private hello
contains
    subroutine hello
    private
    end subroutine

    function FFF() !<<<<< 16, 14, 16, 17, fail-initial
    end function
end program private
