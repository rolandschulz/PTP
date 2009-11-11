MODULE MySeparateFileMod

END MODULE

PROGRAM MyMain

	USE MySeparateFileMod

	COMMON /MyTestFun_common1/ aVar

	REAL :: a_xxx1

	REAL :: comVar

	REAL :: aVar

	COMMON /CB1/ comVar

	print *, test

	print *, internalModVar

	CALL MySeparateSub

	comVar = 5.5

	CONTAINS

	REAL FUNCTION MyTestFun()

		REAL :: com

		COMMON /MyTestFun_common2/ com

		REAL :: q = 3.3, w, e = 5.5

		REAL, DIMENSION(5) :: r, t

		REAL, SAVE :: u = 1.1

		REAL, SAVE :: o

		REAL, POINTER :: p

		POINTER o, p

		POINTER o, p

		REAL :: b, c, d

		REAL, POINTER :: a

		POINTER c, d

		SAVE a, r, p, b, c

		DIMENSION b(10)

		c = 1.2

		MyTestFun = 3.3

	END FUNCTION MyTestFun

	REAL FUNCTION MyTestFun2(aVar)

		REAL, DIMENSION (10:10) :: aVar

		CHARACTER (LEN=30) :: char

		REAL :: bVar(100:100)

		DOUBLE PRECISION :: cVar(10)

		REAL, PARAMETER :: b = 1.1

		REAL c

		POINTER c

		SAVE

	END FUNCTION MyTestFun2

END PROGRAM MyMain

SUBROUTINE MySub

	REAL :: test
	COMMON /CB1/ comVar

	test = 1.1

	comVar = comVar + comVar

END SUBROUTINE MySub
