! Test of a function returning a string -- JO 1/29/09
  print *, c()
contains
  character(len=len("Hello")) function c()
  	c = "Hello"
  end function
end program
