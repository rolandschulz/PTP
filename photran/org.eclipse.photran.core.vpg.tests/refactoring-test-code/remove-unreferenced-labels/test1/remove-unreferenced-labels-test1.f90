program main
  integer ::i
      i=1
 100  if (i.lt.10) then !<<<<< 1, 1, 19, 10, pass
         i=1
 101     continue
 110  else
      end if

end program

900   subroutine OneSubroutine
          return
      end subroutine

      integer function OneFunc()
      994 OneFunc=1
      996 return
      end