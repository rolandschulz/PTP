do
   read (iun, '(1x, g14.7)', iostat = ios) x
   if(ios /= 0) exit
   if(x<0.) cycle
   call suba(x)
   call subb(x)
   call subz(x)
end do

end

