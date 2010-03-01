! Demonstrates operator overloading
! J. Overbey 1 Feb 2010
module m
    implicit none

    type t
      integer :: component
    end type

    ! Overload the less-than operator for derived type t
    interface operator(.lt.)
      module procedure lessthan
    end interface
contains
    logical function lessthan(a, b)
       type(t), intent(in) :: a
       type(t), intent(in) :: b
       lessthan = a%component < b%component
    end function
end module m

program op
    use m
    implicit none

    type(t) :: t1, t2
    t1%component = 1
    t2%component = 2

    ! Invoke overloaded operator using .lt. syntax
    if (t1 .lt. t2) then; print *, 'Yes'; else; print *, 'No'; end if
    if (t2 .lt. t1) then; print *, 'Yes'; else; print *, 'No'; end if

    ! Invoke overloaded operator using < syntax
    if (t1 < t2) then; print *, 'Yes'; else; print *, 'No'; end if
    if (t2 < t1) then; print *, 'Yes'; else; print *, 'No'; end if
end program op
