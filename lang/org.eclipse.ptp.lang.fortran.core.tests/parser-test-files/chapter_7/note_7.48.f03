real :: a(10,10), b(10,10) = 1.0
forall (i=1:10, j=1:10, b(i,j) /= 0.0)
   a(i,j) = real(i+j-2)
   b(i,j) = a(i,j) + b(i,j) * real(i*j)
end forall

end

