subroutine ext1 !1,12
end subroutine ext1 !2,16

program test
  interface
    subroutine ext1 !6,16
    end subroutine ext1 !7,20

    character(len=3) function ext2(i) !9,31
      real, intent(in) :: i
    end function ext2 !11,18

    integer function ext3() !13,22
    end function ext3 !14,18
  end interface

  call ext1 !17,8
  print *, ext2(1.1) !18,12
  print *, ext3() !19,12

end program

function ext3() result(j) !23,10
  j = 3
end function ext3 !25,14
