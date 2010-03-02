program TestExtractVariable

contains

subroutine Sub()
  integer :: x
  x = 1
  if(x==1) then
  	x=2
  end if	
  
end subroutine Sub

end program TestExtractVariable
