module LinearFunction

  interface evaluate
    module procedure evaluate_lf
  end interface evaluate

contains

function evaluate_lf(x)
  implicit none
  real(kind(1.0D0)) :: evaluate_lf
  real(kind(1.0D0)), intent(in) :: x
  evaluate_lf = 12.0 * x + 3.2
end function evaluate_lf

end module LinearFunction
