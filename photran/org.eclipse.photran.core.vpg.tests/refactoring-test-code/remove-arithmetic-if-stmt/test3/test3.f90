program test3 !<<<<< 1,1,pass

INTEGER :: chicken = 0
INTEGER :: rice

! This is a comment before the refactored line
if (chicken) 10,20,30 ! This is a comment on the refactored line
! This is a comment after the refactored line

10 rice = 1
	goto 40
20 rice = 2
	goto 40
30 rice = &
	& 3

40 print *, rice

end program test3