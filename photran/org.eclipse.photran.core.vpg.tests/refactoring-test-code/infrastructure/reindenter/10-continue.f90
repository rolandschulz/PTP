program p !<<<<<START
      implicit none

lbl:  DO I = 1, 10
      end do

      do 100 i = 1, 10
          print *, i
100           CONTINUE

      print *, "Done"
      stop
end program !<<<<<END
