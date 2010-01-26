program unused_variables !<<<<<1,1,true
    implicit none
    integer x,y,z !Integers are declared
    real :: a,b,c !Reals are declared
    y = 6 !Only y is used so far
    z = y + 9 !Now z is used as well
    a = 7.0 !Value is assigned to variable a
end program unused_variables
