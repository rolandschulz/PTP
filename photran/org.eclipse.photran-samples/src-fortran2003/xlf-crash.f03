!>
!! Example program causes an internal compiler error in XLF
!!
!! Jeff Overbey (1/29/09)
!<
module m
  type string
    character, dimension(:), pointer :: data
  contains
    procedure value
  end type
contains
  ! XLF internal compiler error due to this expression
  !             vvvvvvvvvvvvvvvvvvvv
  character(len=ubound(self%data, 1)) function value(self) !result(result)
    class(string), intent(in) :: self
    value = ''
  end function
end module
