! added the rhs to each of these so they would be complete enough 
! to try and parse.
scalar_parent%scalar_field = scalar_parent%scalar_field
array_parent(j)%scalar_field = array_parent(j)%scalar_field
array_parent(1:n)%scalar_field = array_parent(1:n)%scalar_field

end
