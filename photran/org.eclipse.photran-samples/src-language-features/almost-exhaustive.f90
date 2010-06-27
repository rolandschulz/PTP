module indirect
    integer :: included_indirectly_in_points
contains
    subroutine indirect_subroutine
        print *, included_indirectly_in_points
    end subroutine
end module

module points
    use indirect
    implicit none
    type point
        doubleprecision  :: x
        double precision :: y
    end type

    interface operator(+)
        module procedure add
    end interface

    interface operator(.norm.)
        module procedure norm
    end interface

    interface assignment(=)
        module procedure point_from_int, point_from_complex
    end interface

    private :: add, norm
contains
    type(point) function add(p1, p2) result(result)
        type(point), intent(in) :: p1, p2
        result = point(p1%x+p2%x, p1%y+p2%y)
    end function

    function norm(p)
        type(point), intent(in) :: p
        integer :: norm
        norm = abs(p%x**2 + p%y**2)
    end function
!
!    function point_from_int(n)
!        integer, intent(in) :: n
!        type(point) :: point_from_int
!        point_from_int = point(n, 0)
!    end function

    subroutine point_from_int(p, n)
        integer, intent(in) :: n
        type(point), intent(out) :: p
        p = point(n, 0)
    end subroutine

    subroutine point_from_complex(p, c)
        complex, intent(in) :: c
        type(point), intent(out) :: p
        p = point(real(c), aimag(c))
    end subroutine
end module

program Main
	implicit none

	call expressions
	call pointers
	call control
	stop 'Program terminated normally'

contains

    subroutine expressions
	    use points
	    implicit none

	    type(point) :: pt

	    integer, parameter :: kind6=selected_int_kind(6) ! Kind for range [-999999,999999]
	    integer, parameter :: n1 = -123456_kind6
	    integer, parameter :: n2 = -123456_4
	    integer, parameter :: long = selected_real_kind(9, 99) ! 9 sig decimals, exponent
	                                                           ! range 10^-99 to 10^99
	    integer, parameter :: asci = kind('ASCII')
	    integer(kind=long) :: a
	    character(len=20, kind=1) :: english_word
	    character(20) :: length_twenty

	    type person
	        character(10) :: name
	        real          :: age
	        integer       :: id
	    end type
	    type(person) :: me = person('Jeff', 23, 12345)
	    type(person) :: you

		integer i

	    real, dimension(10) :: array1thru10
	    real, dimension(-10:5, 5) :: matrixneg10and5
	    real, dimension(-10:5, -20:-1, 2) :: threedarray
	    real, dimension(5) :: arrayconst

	    character(len=10) :: ten = "1234567890"

	    real, pointer :: realptr => null()

	    you%name = 'Bob'
	    you%age  = 17.25
	    you%id   = 18

	    print *, "Kinds:", kind(kind6), kind(n1), kind(n2), kind(1.0)
	    print *, "Precision:", precision(1.0_long) ! will be at least 9
	    print *, "Num decimal digits supported:", range(2_kind6)
	    print *, "Num decimal digits supported:", range(1.0_long) ! will be at least 99
	    !ERROR!print *, "Bin/octal/hex:", b'01100110', o'076543', z'10fa'
	    print *, "Real literal constant:", -10.6e-11, 1., -0.1, 1e-1, 3.141592653
	    print *, "Complex:", (1., 3.2), (1, .99e-2), (1.0, 3.7_8)
	    print *, 'He said "Hello"', "This contains an '", 'Isn''t it a nice day'
	    print *, 'This is a long string     &
	            & which spans several lines &
	            & unnecessarily.'
	    print *, asci_"String"
	    print *, .false._1, .true._long
	    print *, ten(:5), ten(6:), ten(3:7), ten(:), you%name(1:2), 'Hello'(:2)

	    print *, 2**3*4/5+6-7.8

	    if (1 .lt. 2) print *, "1"; if (1 <  2) print *, "1"
	    if (1 .le. 2) print *, "2"; if (1 <= 2) print *, "2"
	    if (1 .eq. 2) print *, "3"; if (1 == 2) print *, "3"
	    if (1 .ne. 2) print *, "4"; if (1 /= 2) print *, "4"
	    if (1 .gt. 2) print *, "5"; if (1 >  2) print *, "5"
	    if (1 .ge. 2) print *, "6"; if (1 >= 2) print *, "6"

	    print *, .not. .false. .and. (.true. .eqv. .false. .or. .false. .neqv. .true.)
	    print *, "Hello" // "HelloWorld"(6:10)
	    print *, point(1.2d0, 3.4)
	    print *, point(1,2) + point(3,4)
	    print *, .norm. point(5,6)

	    pt = 3; print *, pt
	    pt = (5,6); print *, pt

 		arrayconst = (/ 1, 2, (i+2, i=1,3) /)
		print *, arrayconst

	    matrixneg10and5 = -5; print *, matrixneg10and5(-10,1)
	    matrixneg10and5(-9:-7,1) = -3; print *, matrixneg10and5(-10:-5,1)

	    included_indirectly_in_points = 12
	    call indirect_subroutine()
    end subroutine

    subroutine pointers
		integer, target :: array
		dimension :: array(3)

		integer, dimension(:), pointer :: pointer

		pointer => null()
		print *, associated(pointer)

		pointer => array
		print *, associated(pointer)
		print *, associated(pointer, array)

		nullify (pointer)
		print *, associated(pointer)

		allocate (pointer(5:10))
		print *, associated(pointer)
		print *, associated(pointer, array)
		deallocate (pointer)
    end subroutine

    subroutine control
		integer i, j

    	go to 100

100		continue

		if (sin(3.1415) < 2) print *, "!"

		if (.true.) then
			print *, "if1"
		end if

if2:	if (.true.) then
			print *, "if2"
		end if if2

if3:	if (.false.) then
			print *, "X"
		else if (cos(0.0) == 0) then
			print *, "X"
		else
			print *, "if3"
		end if if3

sc1:	select case (int(cos(0.0)))
		case (-5:0) sc1
			print *, "X"
		case (1)
			print *, "sc1"
		case default sc1
			print *, "X"
		end select sc1

		do 200 i = 1, 3
			do 200 j = 1, 5, 2
				print *, i*10 + j
200		continue

do1:	do i = 1, -3, -1
			if (i == 1) cycle
			do, j = 100, 100
				if (i == 2) cycle do1
				print *, i + j
				if (i == -2) exit do1
				print *, "..."
			end do
		enddo do1
    end subroutine

end program
