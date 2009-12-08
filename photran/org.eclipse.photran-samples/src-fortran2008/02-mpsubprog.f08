! Demonstration of separate module subprograms in Fortran 2008
! Exercises R214, R1108, 1226, 1237-1239
! J. Overbey - 2 Dec 2009

module module
  implicit none

  interface

    integer function twice(n)
      integer, intent(in) :: n
    end function twice
    integer function one(); end function
    integer function two(); end function
    integer function three(); end function
    integer function four(); end function
    integer function five(); end function
    subroutine noop; end subroutine

  end interface

end module module

submodule (module) submodule1
  implicit none
contains

  module procedure twice
                   twice = 2 * n  ! I guess n exists here...  (??)
                                     end
  module procedure one;   one = 1;   endprocedure
  module procedure two;   two = 2;   end procedure
  module procedure three; three = 3; endprocedure three
  module procedure four;  four = 4;  end procedure four
  module function five()
    five = 5
  end function
  module subroutine noop
  end subroutine

  impure function impure()
  end function

end
