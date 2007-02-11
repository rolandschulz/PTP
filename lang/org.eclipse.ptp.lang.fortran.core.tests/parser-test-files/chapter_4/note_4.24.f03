!          NOTE 4.24
!          The following example uses derived-type parameters.

                 TYPE humongous_matrix(k, d)
                   INTEGER, KIND :: k = kind(0.0)
                   INTEGER(selected_int_kind(12)), LEN :: d
                     !-- Specify a nondefault kind for d.
                   REAL(k) :: element(d,d)
                 END TYPE

!          In the following example, dim is declared to be a kind parameter, allowing generic overloading of
!          procedures distinguished only by dim.

                 TYPE general_point(dim)
                   INTEGER, KIND :: dim
                   REAL :: coordinates(dim)
                 END TYPE
end
