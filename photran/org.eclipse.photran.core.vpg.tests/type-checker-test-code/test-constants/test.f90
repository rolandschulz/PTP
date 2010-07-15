type point
  integer x, y
end type

type(point) :: pt




print *, 3   !<<<<< 10, 10, 10, 11, integer
print *, 3.4 !<<<<< 11, 10, 11, 13, real
!print *, point(3, 4)  ! 12, 10, 12, 21, type(point)
end
