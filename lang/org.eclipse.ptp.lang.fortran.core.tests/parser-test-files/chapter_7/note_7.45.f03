where (temp > 100.0) temp = temp - reduce_temp
where (pressure <= 1.0)
   pressure = pressure + inc_pressure
   temp = temp - 5.0
elsewhere
   raining = .true.
end where

end
