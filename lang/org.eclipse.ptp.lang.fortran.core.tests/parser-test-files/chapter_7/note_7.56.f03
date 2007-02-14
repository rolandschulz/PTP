integer a(3,3)
forall(i=1:n-1)
   forall ( j=i+1:n)
      a(i,j) = a(j,i)
   end forall
end forall

end
