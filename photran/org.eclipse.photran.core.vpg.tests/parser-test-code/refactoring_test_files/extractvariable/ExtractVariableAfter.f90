program TestExtractVariable

contains

subroutine Sub()
  integer :: x
  logical :: y
  x = 1
  y = (x == 1)
  if(y) then
  	x=2
  end if	
  
end subroutine Sub

end program TestExtractVariable
