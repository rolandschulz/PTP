type point
   real :: x,y
end type point
type, extends(point) :: point_3d
   real :: z
end type point_3d
type, extends(point) :: color_point
   integer :: color
end type color_point

type(point), target :: p
type(point_3d), target :: p3
type(color_point), target :: c
class(point), pointer :: p_or_c
p_or_c => c
select type (a=>p_or_c)
class is(point)
   print *, a%x, a%y
type is (point_3d)
   print*, a%x, a%y, a%z
end select

end

