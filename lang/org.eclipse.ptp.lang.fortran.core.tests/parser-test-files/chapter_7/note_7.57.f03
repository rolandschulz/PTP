forall (i=1:n) a(i,i) = x(i)

forall(i=1:n, j=1:n) x(i,j) = 1.0 / real(i+j-1)

forall(i=1:n, j=1:n, y(i,j) /= 0 .and. i /= j) x(i,j) = 1.0 / y(i,j)

forall(i=1:n-1, j=1:n, j>i) a(i,j) = a(j,i)

end
