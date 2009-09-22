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

    function FFF()
    end function
end program private
