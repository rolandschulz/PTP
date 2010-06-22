program test1 !<<<<< 1,1,pass

INTEGER :: chicken = 0
INTEGER :: rice

if (chicken) 10,20,30

10 rice = 1
	goto 40
20 rice = 2
	goto 40
30 rice = &
	& 3

40 print *, rice

end program test1