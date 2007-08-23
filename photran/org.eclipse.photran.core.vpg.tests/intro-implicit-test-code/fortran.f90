program functioncalling
  print *, 'This is the Fortran program; I am going to call some functions now...'
  call sum(1.0,2.0,3.0)
  call factorial(3)
  print *, 'Done'

contains

  subroutine sum(x,y,z)
  
  
	  x = x+y+z
	  print *, x
  end subroutine sum

  subroutine factorial(j)
	  p=1
	  i=1
	  do i=1,j
	    p=p*i
	  end do
	
	  print *, j, "! = ", p
  end subroutine factorial

  subroutine sum2(x,y)
	  implicit none
	  real :: p
	  integer :: x, y
	  p = x+y
  end subroutine sum2
end
