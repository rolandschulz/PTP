character(80) :: line

level = 0
scan_line: do i = 1,80
   check_parens: select case(line(i:i))
   case('(')
      level = level + 1
   case(')')
      level = level - 1
      if(level < 0) then
         print *, 'unexpected right parenthesis'
         exit scan_line
      end if
      case default
         ! ignore all other characters
      end select check_parens
   end do scan_line
if(level > 0) then
   print *, 'missing right parenthesis'
end if

end

