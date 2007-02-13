if(cvar == 'reset') then
   i = 0; j = 0; k = 0
end if
proof_done: if(prop) then
   write(3,'(''qed'')')
   stop
else
   prop = nextprop
end if proof_done
if(a>0) then
   b = c/a
   if(c>0) then
      d = 1.0
   end if
else if(c>0) then
   b = a/c
   d = -1.0
else
   b = abs(max(a,c))
   d = 0
end if

end

