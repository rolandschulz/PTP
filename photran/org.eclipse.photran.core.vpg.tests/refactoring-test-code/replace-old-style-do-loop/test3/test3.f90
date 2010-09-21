program Test3 !<<<<< 1, 1, 14, 18, pass
    do 100 i = 1,10
    do 110 j = 1,10
    do 120 k = 1,10
    do 130 l = 1,10
    do 140 m = 1,10
    do 150 n = 1,10
    150 x=i+j+k+l+m+n
    140 y=j+k+l+m+n
    130 z=k+l+m+n
    120 w=l+m+n
    110 t=m+n
    100 v=n
end program Test3