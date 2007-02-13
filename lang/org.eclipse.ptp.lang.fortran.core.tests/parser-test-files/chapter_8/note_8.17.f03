read (iun, '(1x, g14.7)', iostat = ios) x
do while (ios == 0)
   if(x>=0.) then
      call suba(x)
      call subb(x)
      call subz(x)
   endif
   read(iun, '(1x, g14.7)', iostat = ios) x
end do

end
