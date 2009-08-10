module mod
  implicit none

  type point
    double precision x, y, z
  end type

  type(point) :: variable  !8,18,8
end module

program encap1
  use mod
  implicit none
  print *, variable
  variable = point(1.0, 2.0, 3.0)
  print *, variable
end program encap1
