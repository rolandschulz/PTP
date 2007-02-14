module mod1
type initialized_type
   integer :: i = 1
end type initialized_type

save :: saved1, saved2
integer :: saved1, unsaved1
type(initialized_type) :: saved2, unsaved2
allocatable :: saved1(:), saved2(:), unsaved1(:), unsaved2(:)
end module mod1

program main

  call sub1
  call sub1

contains
  subroutine sub1
    use mod1
    ! in the draft, this should use the continuation line.  however, this 
    ! is not yet implemented, so will need to put this back later.
    ! 11.09.06
!     print *, allocated(saved1), allocated(saved2), &
!          allocated(unsaved1), allocated(unsaved2)
    print *, allocated(saved1), allocated(saved2), allocated(unsaved1), allocated(unsaved2)
    if(.not. allocated(saved1)) allocate(saved1(10))
    if(.not. allocated(saved2)) allocate(saved2(10))
    if(.not. allocated(unsaved1)) allocate(unsaved1(10))
    if(.not. allocated(unsaved2)) allocate(unsaved2(10))
  end subroutine sub1
end program main
    
