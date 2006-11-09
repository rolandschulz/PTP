!
! R701 primary (s)
!

a=1.0					! constant
a='ABCDEFGHIJKLMNOPQRSTUVWXYZ' (I:I)	! constant-subobject
a='ABC"""DEFGHIJKLM""NOPQRSTUVWXYZ' (I:I)	! constant-subobject
a='ABC"""DEFGHIJKLM""NOPQRSTUVWXYZ' (I:I)	! constant-subobject
a="ABCDEFGHIJKLMNOPQRSTUVWXYZ" (I:I)	! constant-subobject
a=A					! variable is designator
a=(/ 1.0, 2.0 /)			! array-constructor
a=PERSON (12, 'Jones')			! structure-constructor
a=PERSON (12, "Jones")			! structure-constructor
a=F (X, Y)				! function-reference
a=(S + T)				! (expr)

a = 'don''t'
b = 'don""""''t'
c = 'i''like''f''o''rtra''''n'
d = "another""test"
e = "another"" """"test"



END
