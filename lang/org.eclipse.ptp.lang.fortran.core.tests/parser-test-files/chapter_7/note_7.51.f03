integer :: x = -1
real a(5,4)
j = 100

forall(x=1:5, j=1:4)
   a(x,j) = j
end forall

end
