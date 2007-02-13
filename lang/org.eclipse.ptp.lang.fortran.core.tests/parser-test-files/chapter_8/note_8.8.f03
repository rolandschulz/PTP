if(silly == 1) then
   call this
else
   call that
end if
select case(silly == 1)
case(.true.)
   call this
case(.false.)
   call that
end select
select case(silly)
case default
   call that
case(1)
   call this
end select

end

