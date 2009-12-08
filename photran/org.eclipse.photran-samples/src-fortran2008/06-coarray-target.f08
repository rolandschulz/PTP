! Exercises the following rules for co-array declarations:
! R557
! J. Overbey - 7 Dec 2009

  implicit none

  ! R557
  target :: a, b(3), c[*]
  target    d(3)[*], e[4, 1*2:*]
end program
