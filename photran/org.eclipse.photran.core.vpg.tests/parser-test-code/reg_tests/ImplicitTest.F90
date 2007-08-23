program lineContTest
	! See Fortran 95 Handbook p.112
	implicit integer (a)
	implicit integer (b-c)
	implicit complex (d-e, f)
	implicit integer (g-i, k), complex(l-n)
	implicit type (mytype) (p), complex (q)
	implicit logical (kind = bit) (r)
	IMPLICIT INTEGER (S-V) ! Comment here
	IMPLICIT CHARACTER(LEN=5)(W-X)
	IMPLICIT CHARACTER(LEN=5, KIND=7)(Y), COMPLEX(Z) ! Yeah!
end program lineContTest
