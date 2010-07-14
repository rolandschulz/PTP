program testio
    integer :: assignme, assignme2, assignme3
    integer :: w,x
    real :: y = 23.4
    real :: z = 25.5
    character(len=5) :: format = "(I5)"
    character(len=50) :: testline

    assign 100 to assignme2
    assign 200 to assignme3

    read (5, format, iostat=w, err=100, end=100) x
    !<<<<< 12,14,READ
    !<<<<< 12,29,RW
    !<<<<< 12,50,WRITE

    assign 20 to assignme
    !<<<<< 17,18,WRITE

    print assignme, x,y,z
    !<<<<< 20,11,READ
    !<<<<< 20,21,READ
    !<<<<< 20,23,READ
    !<<<<< 20,25,READ
    
20  format(i10,f10.2,f10.3)
    !<<<<< 26,12,NONE
    !<<<<< 26,16,NONE
    !<<<<< 26,22,NONE
    
    write (6, format, iostat=w, err=100) x
    !<<<<< 31,15,READ
    !<<<<< 31,30,RW
    !<<<<< 31,42,READ
    
    write (6, "(I5)", iostat=w, err=100) 2
    !<<<<< 36,15,NONE
    !<<<<< 36,30,RW
    !<<<<< 36,42,NONE
    
    write (testline, assignme, iostat=w, err=100) x,y,z
    !<<<<< 41,12,WRITE
    !<<<<< 41,22,READ
    !<<<<< 41,39,RW
    !<<<<< 41,51,READ
    !<<<<< 41,53,READ
    !<<<<< 41,55,READ

100 print *, "1"
200 print *, "2"

end program testio
