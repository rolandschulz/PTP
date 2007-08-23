!            14                             45     52
pure integer(selected_int_kind(3)) function thrice(n)
  intent(in) :: n ! 17    n should be implicitly declared, NOT resolve to the outer n
  thrice=-999 ! 3
end function thrice ! 14

  type type ! 8
    integer :: a ! 16
  end type type ! 12

! 3      10     17  
! thrice(n) = 3*n ! Statement function, shadows outer function
! 3
  n = 1 ! Different n than statement function parameter

  !        12         23     30  34  38    44
  print *, thrice(6), thrice(n), n,  f(1), f(3)
  ! Expect 18.0       3.0        1.0 1.0   3.0
  stop

contains
                           ! 30 33
  recursive integer function f (a)
    !          16
    integer :: a ! Different a than derived type component
    !    10       19
    type(type) :: x
    !    10                28   33
    type(type), pointer :: p => null()
    
    target :: x ! 15
    intent(in) :: a ! 19

!   5 7   11
    x%a = a ! Assign local a to component
!   5   9    14
    x = type(a) ! Structure ctor assigning local a to component a
!   5    10
    p => x
    !p => null() does not parse

!   5
    f = 1
!       9      16  20     2729
    if (a > 1) f = thrice(f(f-1))
  end function f ! 16
end
