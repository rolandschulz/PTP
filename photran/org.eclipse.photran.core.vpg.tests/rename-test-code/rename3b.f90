module module_b ! 1,8
    use module_c ! 2,9
contains
    subroutine b_sub1of3; end subroutine ! 4,16
    subroutine b_sub2of3; end subroutine ! 5,16
    subroutine b_sub3of3; ! 6,16
      call c_sub ! 7,12
    end subroutine
end module
