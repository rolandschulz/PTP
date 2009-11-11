module module1
   implicit none
   private
contains
   ! sub1a cannot be made public w/o ONLY clause in main
   subroutine sub1a
       print *, "sub1a in module1"
   end subroutine
   ! sub1b can be made public
   subroutine sub1b
       print *, "sub1b in module1"
   end subroutine
end module module1

module module2
   implicit none
   ! integer1 and integer3 cannot be made public w/o ONLY clause
   ! integer2 and integer4 can be made public
   integer, private :: integer1, integer2
   integer :: integer3, integer4
   private :: integer4, integer3
end module

program main
   use module1
   use module2
   implicit none
   integer :: integer1, integer3
   call sub1a  ! local, not import
contains
   subroutine sub1a
       print *, "sub1a in main program"
   end subroutine
end program main
