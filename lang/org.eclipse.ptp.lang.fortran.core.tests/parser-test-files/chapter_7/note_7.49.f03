forall (i=1:n, j=1:n)
   a(:, i, :, j) = 1.0 / real(i+j-1)
end forall

end
