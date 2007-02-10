! test if we can correctly recognize the assignment type (between normal or
! a pointer assignment)
i = 3
ptr => i
integer%real%my_type%this_func(1) => real
integer%real%my_type%this_func(1, subroutine) => real
integer%real%my_type%this_func(1) = real
integer%real%my_type%this_func(1, subroutine) = real
end

if(i > j) i = j
if(i < integer) integer = i
if(integer .ge. real) integer = real
end

if(integer.le.real) 10 ,20,30
if(i+j) 10 ,20,30

10 print *, '10'
20 print *, '20'
30 print *, '30'
end
! above this line should work


n = 0
do 100 i = 1,10
   j = i
   do 100 k = 1,5
      l = k
100   n = n + 1


end
