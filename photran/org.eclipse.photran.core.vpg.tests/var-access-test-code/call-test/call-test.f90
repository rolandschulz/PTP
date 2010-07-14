program call
    implicit none
    integer :: i
    integer, dimension(5) :: array

!    interface
!        integer function external_fn(n)
!          integer, intent(in) :: n
!        end function
!        subroutine external_sub(n)
!          integer, intent(out) :: n
!        end subroutine
!    end interface

    call s(i, i, i) 
    !<<<<< 15,12,READ
    !<<<<< 15,15,WRITE
    !<<<<< 15,18,RW
    i = f(i, i, i)
    !<<<<< 19,5,WRITE
    !<<<<< 19,11,READ
    !<<<<< 19,14,WRITE
    !<<<<< 19,17,RW
    i = g(i)
    !<<<<< 24,5,WRITE
    !<<<<< 24,11,WRITE
    array(i) = array(i) * f(i, i, i)
    !<<<<< 27,5,WRITE
    !<<<<< 27,11,READ
    !<<<<< 27,16,READ
    !<<<<< 27,22,READ
    !<<<<< 27,29,READ
    !<<<<< 27,32,WRITE
    !<<<<< 27,35,RW

    call s(q=i, p=i, r=i)
    !<<<<< 36,14,WRITE
    !<<<<< 36,19,READ
    !<<<<< 36,24,RW
    i = f(q=i, p=i, r=i)
    !<<<<< 40,5,WRITE
    !<<<<< 40,13,WRITE
    !<<<<< 40,18,READ
    !<<<<< 40,23,RW

!    call external_sub(i)
!    i = external_fn(i)

    call unknown_subroutine(i, (i), i+i)
    !<<<<< 49,29,RW
    !<<<<< 49,33,READ
    !<<<<< 49,37,READ
    !<<<<< 49,39,READ
    i = unknown_function(i, (i), i+i)
    !<<<<< 54,5,WRITE
    !<<<<< 54,26,RW
    !<<<<< 54,30,READ
    !<<<<< 54,34,READ
    !<<<<< 54,36,READ
contains
    subroutine s(p, q, r)
      integer, intent(in) :: p
      integer, intent(out) :: q
      integer, intent(inout) :: r
    end subroutine s
    integer function f(p, q, r)
      integer, intent(in) :: p
      integer, intent(out) :: q
      integer, intent(inout) :: r
      f = p
      !<<<<< 70,7,WRITE
      !<<<<< 70,11,READ
    end function
    integer function g(i)
      integer, intent(out) :: i
    end function
end program call
