type t1(l)
    integer, len :: l
end type

type t2(len)
    integer, len :: len
end type

!            v-- F03 parser reports unexpected len
integer ifdr,len_n,len_c,filtyp,fdrnam

end program
