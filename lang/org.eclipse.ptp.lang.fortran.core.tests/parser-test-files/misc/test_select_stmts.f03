select type (p_or_c)
class is (point)
   print *, 'class is point'
type is (integer)
   print *, 'type is integer'
end select

select type (p_or_c)
class is (point)
   print *, 'class is point'
type is (integer)
   print *, 'type is integer'
end select

select type (p_or_c)
class is (point)
   print *, 'class is point'
type is (integer(c_int))
   print *, 'type is integer'
end select

real: select type (p_or_c)
class is (point)
   print *, 'class is point'
type is (integer(c_int)) real
   print *, 'type is integer'
end select real

select type (p_or_c)
class is (point)
   print *, 'class is point'
type is (point)
   print *, 'type is integer'
end select

real: select type (p_or_c)
class is (point)
   print *, 'class is point'
type is (point) real
   print *, 'type is integer'
end select real

end
