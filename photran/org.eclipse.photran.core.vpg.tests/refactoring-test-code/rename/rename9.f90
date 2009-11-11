module M1
  implicit none
  integer :: i1 !3,14
  integer, private :: j1 !4,23
  private :: k1 !5,14
  interface
    character(len=3) function k1(z) !7,31
      real, intent(in) :: z
    end function
  end interface
contains
  integer function f1(); f1 = 1; end function !12,20 12,26
end module M1

module M2
  implicit none
  integer :: i2 !17,14
  integer, public :: j2
  private
  public :: k2 !20,13
  interface
    character(len=3) function k2(i2) !22,31
      doubleprecision, intent(in) :: i2
    end function
  end interface
contains
  integer function f2(); f2 = 1; end function !27,20 27,26
end module M2

program p
  use M1
  use M2
!implicit none ! Uncomment to see compile errors (to determine which are implicits)
  ! j1 k1 z implicit
  !        12  16  20  24    30
  print *, i1, j1, k1, f1(), z
  ! i2 f2 implicit
  !        12  16  20       29
  print *, i2, j2, k2(3d0), f2
end program
