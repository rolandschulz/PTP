module CmatProduct_m

use MatranUtil_m
use Cmat_m

implicit none



  real(wp), parameter :: ZERO = 0, ONE = 1

  interface Times
     module procedure CmTimesComplexScalarRm, CmTimesComplexScalarCm, &
     CmTimesRealScalarCm, CmTimesRealScalarRm
  end interface Times

!    interface operator(*)
!       module procedure RmTimesScalarRm_o, RmTimesRmScalar_o, &
!                        RmTimesRmRm_o
!    end interface
!
!    interface TimesXhy
!       module procedure RmTimesXhy
!    end interface TimesXhy
!
!    interface operator(.xhy.)
!       module procedure RmTimes_xhy
!    end interface
!
!    interface TimesXyh
!       module procedure RmTimesXyh
!    end interface TimesXyh
!   !    interface operator(.xyh.)
!       module procedure RmTimes_xyh
!    end interface
!
!    interface TimesXhx
!       module procedure RmTimesXhx
!    end interface
!
!    interface operator(.xhx.)
!       module procedure RmTimes_xhx
!    end interface
!
!    interface TimesXxh
!       module procedure RmTimesXxh
!    end interface
!
!    interface operator(.xxh.)
!       module procedure RmTimes_xxh
!    end interface

contains

  subroutine CmTimesComplexScalarRm(C, s, A)
     type(Cmat), intent(inout) :: C
     complex(wp), intent(in) :: s
     type(Rmat), intent(in) :: A

     integer :: m, n, i, j

     m = A%nrow
     n = A%ncol

     call GuardTemp(A)
     call ReshapeAry(C, m, n)

     if (m==0 .or. n==0) then
        C%tag = 'yy'
     !else                C%tag = 'xx'
     else
                     C%tag = 'xx'
        forall (i=1:m,j=1:n)
            C%a(i,j) = s*cmplx(A%a(i,j),0)
        end forall

     end if

     call CleanTemp(A)

  end subroutine CmTimesComplexScalarRm

  subroutine CmTimesRealScalarCm(C, s, A)
     type(Cmat), intent(out) :: C
     real(wp), intent(in) :: s
     type(Cmat), intent(in) :: A

     integer m, n

     m = A%nrow
     n = A%ncol

     call GuardTemp(A)
     call ReshapeAry(C, m, n)

     if (m==0 .or. n==0) then
        C%tag = 'GE'

     else

        if (s>=0 .OR. A%tag/='HP') then
           C%tag = A%tag
        else
           C%tag = 'HE'
        end if
        C%a(1:m,1:n) = s*A%a(1:m,1:n)

     end if

     call CleanTemp(A)

  end subroutine CmTimesRealScalarCm
 
  subroutine CmTimesRealScalarRm(C, s, A)
     type(Cmat), intent(out) :: C
     real(wp), intent(in) :: s
     type(Rmat), intent(in) :: A

     integer m, n

     m = A%nrow
     n = A%ncol

     call GuardTemp(A)
     call ReshapeAry(C, m, n)

     if (m==0 .or. n==0) then
        C%tag = 'GE'

     else

        if (s>=0 .OR. A%tag/='HP') then
           C%tag = A%tag
        else
           C%tag = 'HE'
        end if
        C%a(1:m,1:n) = s*A%a(1:m,1:n)

     end if

     call CleanTemp(A)

  end subroutine CmTimesRealScalarRm
 
  subroutine CmTimesComplexScalarCm(C, s, A)
     type(Cmat), intent(out) :: C
     complex(wp), intent(in) :: s
     type(Cmat), intent(in) :: A

     integer m, n

     m = A%nrow
     n = A%ncol

     call GuardTemp(A)
     call ReshapeAry(C, m, n)

     if (m==0 .or. n==0) then
        C%tag = 'GE'

     else

        C%tag = 'HE'
        C%a(1:m,1:n) = s*A%a(1:m,1:n)

     end if

     call CleanTemp(A)

  end subroutine CmTimesComplexScalarCm
 


end module CmatProduct_m