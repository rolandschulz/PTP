integer a(5,4), b(5,4)
forall(i=1:5)
   where(a(i,:) == 0) a(i,:) = i
   b(i,:) = i/a(i,:)
end forall

end
