module MonteCarloIntegrator

  interface integrate
    module procedure integrate_mc
  end interface integrate

contains

function integrate_mc(lowBound, upBound, count)
  use LinearFunction, only: evaluate
  use RandNumGenerator, only: getRandomNumber
  implicit none
  real(kind(1.0D0)) :: integrate_mc
  real(kind(1.0D0)), intent(in) :: lowBound, upBound
  integer, intent(in) :: count

  ! LOCAL VARIABLES:

  integer :: i
  real(kind(1.0D0)) :: width, x, sum = 0.0D0

  integrate_mc = 0.0D0
           
  ! Compute integral

  width = upBound - lowBound
  do i = 1, count
     x = getRandomNumber()
     x = lowBound + width*x
     sum = sum + evaluate(x)
  end do

  integrate_mc = width*sum/count

end function integrate_mc

end module MonteCarloIntegrator
