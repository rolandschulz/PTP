p_or_c => p3
select type (p_or_c)
class is (point)
   print *, p_or_c%x, p_or_c%y
type is (point_3d)
   print *, p_or_c%x, p_or_c%y, p_or_c%z
end select

end
