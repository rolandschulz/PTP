module temperature
  real, protected :: temp_c, temp_f
contains
  subroutine set_temperature_c(c)
    real, intent(in) :: c
    temp_c = c
    temp_f = temp_c*(9.0/5.0) + 32
  end subroutine set_temperature_c
end module temperature
  
