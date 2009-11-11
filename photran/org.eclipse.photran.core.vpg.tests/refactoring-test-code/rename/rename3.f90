program MyProgram ! 1,9
  use module_a, a_renamed3 => a_sub3of3 ! 2,7 2,17 2,31
  use module_b, only: b_sub2of3, b_renamed3 => b_sub3of3 ! 3,7 3,23 3,34 3,48

  implicit none

  call a_sub1of3  !  7,8
  call a_sub2of3  !  8,8
  call a_renamed3 !  9,8
  call b_sub2of3  ! 10,8
  call b_renamed3 ! 11,8
  call contained  ! 12,8
  call external   ! 13,8
contains
  subroutine contained; end subroutine ! 15,14
end

subroutine external; end subroutine ! 18,12
