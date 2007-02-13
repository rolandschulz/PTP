associate(z=>exp(-(x**2+y**2)) * cos(theta))
  print *, a+z, a-z
end associate

associate(xc => ax%b(i,j)%c)
  cx%dv = xc%dv + product(xc%ev(1:n))
end associate

associate (array => ax%b(i,:)%c)
  array(n)%ev = array(n-1)%ev
end associate

associate (w => result(i,j)%w, zx => ax%b(i,j)%d, zy => ay%b(i,j)%d)
  w = zx*x + zy*y
end associate

end
