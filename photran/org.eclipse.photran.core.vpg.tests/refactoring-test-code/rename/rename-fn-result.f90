  print *, f()   ! 1,12
contains
  function f()   ! 3,12
    integer :: f ! 4,16
    f = 3        ! 5,5
  end function f ! 6,16
end program
