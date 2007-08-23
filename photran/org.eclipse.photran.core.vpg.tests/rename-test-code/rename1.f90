program Main
    integer, parameter :: two = 2
    integer, parameter :: three = selected_real_kind(3)
    integer :: a, b = three
    integer(three) :: not_shadowed = 98765
    real(kind=three) :: c
    complex :: shadow_this_1 = (three, two), shadow_this_2 = (2, three)
    implicit = 13579
    print *, a, b, c, two, three, not_shadowed, shadow_this_1, shadow_this_2, implicit
    
    call int
    call ext
    stop
    
contains

    subroutine int
      complex :: shadow_this_1
      integer :: shadow_this_2
      
      print *, not_shadowed, shadow_this_1, shadow_this_2, implicit
      call ext
    end subroutine int
    subroutine sub
    end subroutine sub
end program Main
subroutine ext
    print *, two
contains
    subroutine int
        print *, two
    end subroutine int
end subroutine ext
