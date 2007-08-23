program main
  use MonteCarloIntegrator, only: integrate

  integer :: count = 100
  real(kind(1.0D0)) :: lb = 0.0D0, ub = 1.0D0

  print *, "Integral value = ", integrate(lb, ub, count)

end program
