end
! f : (((double -> char(3)), double) -> char(3)) -> char(*)
!        1012        22
function f(g) result(q)
  implicit none
  !                   23
  character(len=*) :: q
  interface
    ! g : ((double -> char(3)), double) -> char(3)
    !                         3133 36
    character(len=3) function g(h, x)
      interface
        ! h : double -> char(3)
        !                         3537
        character(len=3) function h(x)
          !                               43
          double precision, intent(in) :: x
        end function h !<<< 22
      end interface
      !                               39
      double precision, intent(in) :: x
    !            18
    end function g
  end interface
! 3
  q = 'This is terrible'
!            14
end function f
!                18   23                  43
integer function g(); g = 3; end function g
