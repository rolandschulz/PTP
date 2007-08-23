! This is a Fortran file
! for testing
program proggie ! Yes, a program
	integer, parameter :: TWENTY = 20 ! a constant
	print *, "Kon'nichiwa, Photran refactoring engine!"
contains
  
  ! Subroutine comment
  subroutine s(x) ! sub sub
    real, intent(in) :: x ! This is x
    print *, x ! Printing
  end subroutine
  
  subroutine r
  end
end

subroutine t
end subroutine ! EOF comment
subroutine q
end