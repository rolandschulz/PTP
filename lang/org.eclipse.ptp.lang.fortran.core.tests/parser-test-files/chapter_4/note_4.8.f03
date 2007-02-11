!         NOTE 4.8

!         On a processor that can distinguish between 0.0 and -0.0,

if              ( X >= 0.0 )    x = 0.0

!         evaluates to true if X = 0.0 or if X = -0.0,

if              ( X < 0.0 )     x = 0.0

!         evaluates to false for X = -0.0, and

              IF (X) 1,2,3
1 continue
2 continue
3 continue

end



