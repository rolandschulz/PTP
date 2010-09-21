module class_test

 private :: s

 integer :: a

 contains

 subroutine s !<<<<<9,2,9,12,pass
  integer ::a

	end subroutine


end module class_test
program main; use class_test; a = 2; end program
