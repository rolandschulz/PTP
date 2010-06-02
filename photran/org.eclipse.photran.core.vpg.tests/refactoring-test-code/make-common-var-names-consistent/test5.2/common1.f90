program common1
!use common2
!use common3
!use common4
implicit none

common /block/ a, b, c, /mem/ r, f, t !<<<<< 7, 9, 7, 11, help_common2, b_hello, c_hello, fail-initial
integer :: a
real :: b
double precision :: c

integer :: r, f, t

a = 5
b = 4.6
c = 2.345

call helper
end program common1

subroutine helper
implicit none
common /block/ e, f, g
integer :: e
real :: f
double precision :: g
end subroutine helper
