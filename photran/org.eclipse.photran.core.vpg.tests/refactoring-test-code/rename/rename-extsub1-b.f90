character(len=3) function ext2(i) !1,27
  real, intent(in) :: i
  external ext3 !3,12

  j = ext3() !5,7
  ext2 = 'Bye' !6,3
end function ext2 !7,14
