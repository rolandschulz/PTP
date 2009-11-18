implicit none
integer :: three = 3; integer :: only_once = 0 !<<<<<2,34,z,true, !<<<<<2,34,three,false,
print *, three ! FIXME 3,10,x,true, ! FIXME 3,10,abcdefghijklmnop,true,
print *, three + (three * 3 - three)
end program
