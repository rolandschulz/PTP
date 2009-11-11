interface
  integer function f() ! 2,20
  end function
end interface

contains

  subroutine s1(f)
    interface
      integer function f() ! 10,14 Subroutine parameter -- should not bind
      end function
    end interface
  end subroutine

  subroutine s2
    interface
      integer function f() ! 17,24
      end function
    end interface
  end subroutine

end program

integer function f() ! 24,18
  f = 0
end function
