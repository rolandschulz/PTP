!>
!! Definition of a string class for Fortran.
!!
!! Jeff Overbey (1/29/09)
!<
module string_module
    implicit none
    private

    public :: new_string

    interface new_string
        module procedure &
            new_string_string, &
            new_string_integer, &
            new_string_logical
    end interface

    type, public :: string
        private
        character, dimension(:), allocatable :: data
        integer :: length = 0
    contains
        procedure :: initialize
        generic   :: operator(+) => concat1, concat2
        generic   :: concat => concat1, concat2  ! Photran doesn't parse - R1207
        procedure :: substring
        procedure :: char_at
        procedure :: map
        procedure :: to_uppercase
        procedure :: to_lowercase
        generic   :: write(formatted) => write_formatted    ! Photran doesn't parse
        generic   :: write(unformatted) => write_unformatted
        procedure :: value
        generic   :: operator(==) => equals
        generic   :: operator(.eqic.) => equals_ignore_case
        final     :: destructor

        procedure, private :: concat1
        procedure, private :: concat2
        procedure, private :: write_formatted
        procedure, private :: write_unformatted
        procedure, private :: all_satisfy
        procedure, private :: equals
        procedure, private :: equals_ignore_case
    end type string

contains

    type(string) function new_string_string(string) result(return)
        character(len=*), intent(in) :: string
        call return%initialize(string)
    end function

    type(string) function new_string_integer(integer) result(return)
        integer, intent(in) :: integer
        return = new_string_nat(abs(integer))
        if (integer < 0) return = new_string("-") + return
    end function

    type(string) function new_string_logical(logical) result(return)
        logical, intent(in) :: logical
        if (logical) then
            return = new_string('.true.')
        else
            return = new_string('.false.')
        end if
    end function

    type(string) recursive function new_string_nat(absval) result(return)
        integer, intent(in) :: absval

        if (0 <= absval .and. absval <= 9) then
            call return%initialize(digit_string(absval))
        else
            return = new_string_nat(absval / 10) + digit_string(mod(absval, 10))
        end if
    end function

    character(len=1) function digit_string(digit)
        integer, intent(in) :: digit
        select case (digit)
            case (0); digit_string = '0'
            case (1); digit_string = '1'
            case (2); digit_string = '2'
            case (3); digit_string = '3'
            case (4); digit_string = '4'
            case (5); digit_string = '5'
            case (6); digit_string = '6'
            case (7); digit_string = '7'
            case (8); digit_string = '8'
            case (9); digit_string = '9'
        end select
    end function

    subroutine initialize(self, data)
        class(string), intent(inout) :: self
        character(len=*), intent(in) :: data
        integer :: i

        self%length = len(data)
        allocate(self%data(self%length))
        do i = 1, len(data)
            self%data(i) = data(i:i)
           end do
    end subroutine

    ! XLF parses class(string) but gives semantic error; Photran doesn't parse
    type(string) function concat1(self, other) result(return)
        class(string), intent(in) :: self
        class(string), intent(in) :: other

        return = concat2(self, other%value())
    end function

    type(string) function concat2(self, other) result(return)
        class(string), intent(in) :: self
        character(len=*), intent(in) :: other
        integer :: i

        return%length = self%length + len(other)
        allocate(return%data(return%length))
        return%data(1:self%length) = self%data
        do i = 1, len(other); return%data(i + self%length) = other(i:i); end do
    end function

    type(string) function substring(self, start, thru) result(return)
        class(string), intent(in) :: self
        integer, intent(in) :: start, thru

        if (thru < start) return

        return%length = thru - start + 1
        allocate(return%data(return%length))
        return%data(1:self%length) = self%data(start:thru)
    end function

    character(len=1) function char_at(self, index) result(return)
        class(string), intent(in) :: self
        integer, intent(in) :: index
        return = self%data(index)
    end function

    type(string) function map(self, function) result(return)
        class(string), intent(in) :: self
        interface
            character(len=1) function function(string)
              character(len=1), intent(in) :: string
            end function
        end interface
        integer :: i

        return%length = self%length
        allocate(return%data(return%length))
        do i = 1, self%length
             return%data(i) = function(self%data(i))
        end do
    end function

    type(string) function to_uppercase(self) result(return)
        class(string), intent(in) :: self
        return = self%map(to_upper)
    !contains ! Causes XLF compiler bug -- see xlf-bug.f03
    end function
        character(len=1) function to_upper(ch) result(return)
            character(len=1), intent(in) :: ch
            integer, parameter :: diff = iachar('a') - iachar('A')

            if (lle('a', ch) .and. lle(ch, 'z')) then
                return = achar(iachar(ch) - diff)
            else
                return = ch
            end if
        end function
    !end function

    type(string) function to_lowercase(self) result(return)
        class(string), intent(in) :: self
        return = self%map(to_lower)
    !contains ! Causes XLF compiler bug -- see xlf-bug.f03
    end function
        character(len=1) function to_lower(ch) result(return)
            character(len=1), intent(in) :: ch
            integer, parameter :: diff = iachar('a') - iachar('A')

            if (lle('A', ch) .and. lle(ch, 'Z')) then
                return = achar(iachar(ch) + diff)
            else
                return = ch
            end if
        end function
    !end function

    subroutine write_formatted(self, unit, iotype, v_list, iostat, iomsg)
        class(string), intent(in) :: self
        integer, intent(in) :: unit
        character(*), intent(in) :: iotype
        integer, dimension(:), intent(in) :: v_list
        integer, intent(out) :: iostat
        character(*), intent(inout) :: iomsg
        ! TODO Incomplete
        write (unit=unit, fmt=*, iostat=iostat, iomsg=iomsg) self%value()
    end subroutine

    subroutine write_unformatted(self, unit, iostat, iomsg)
        class(string), intent(in) :: self
        integer, intent(in) :: unit
        integer, intent(out) :: iostat
        character(*), intent(inout) :: iomsg
        ! TODO Incomplete
        write (unit=unit, fmt=*, iostat=iostat, iomsg=iomsg) self%value()
    end subroutine

    character(len=self%length) function value(self) result(result)
        class(string), intent(in) :: self
        integer :: i

        do i = 1, ubound(self%data, 1)
            result(i:i) = self%data(i)
        end do
    end function

    logical function all_satisfy(self, function, other) result(return)
        class(string), intent(in) :: self
        interface
            logical function function(ch1, ch2)
                character(len=1), intent(in) :: ch1, ch2
            end function
        end interface
        class(string), intent(in) :: other
        integer :: i

        if (self%length <> other%length) then
            return = .false.
        else
            return = .true.
            do i = 1, self%length
                if (function(self%data(i), other%data(i)) .eqv. .false.) then
                    return = .false.
                    return
                end if
            end do
        end if
    end function

    logical function equals(self, other) result(return)
        class(string), intent(in) :: self
        class(string), intent(in) :: other
        return = self%all_satisfy(ch_eq, other)
    end function

    logical function ch_eq(ch1, ch2) result(return)
        character(len=1), intent(in) :: ch1, ch2
        return = ch1 .eq. ch2
    end function

    logical function equals_ignore_case(self, other) result(return)
        class(string), intent(in) :: self
        class(string), intent(in) :: other
        return = self%all_satisfy(ch_eqic, other)
    end function

    logical function ch_eqic(ch1, ch2) result(return)
        character(len=1), intent(in) :: ch1, ch2
        return = to_upper(ch1) .eq. to_upper(ch2)
    end function

    subroutine destructor(self)
        type(string), intent(inout) :: self
    end subroutine

end module string_module

module test_util
    implicit none
contains
    character(len=1) function identity(string) result(return)
        character(len=1), intent(in) :: string
        return = string
    end function

    character(len=1) function always_a(string) result(return)
        character(len=1), intent(in) :: string
        return = 'a'
    end function
end module test_util

program string_test
    use string_module
    use test_util
    implicit none

    type(string) :: s1, s2

    print *, "Expected: "
    print *, "Actual:   ", s1%value()

    call s1%initialize('Hello')
    call s2%initialize('!')

    print *, "Expected: Hello"
    print *, "Actual:   ", s1%value()

    s1 = new_string("HeLlOoO!")
    print *, "Expected: HeLlOoO!"
    print *, "Actual:   ", s1%value()
    s1 = new_string("Hello")

    s1 = s1%concat(', world')
    print *, "Expected: Hello, world"
    print *, "Actual:   ", s1%value()

    s1 = s1%concat(s2)
    print *, "Expected: Hello, world!"
    print *, "Actual:   ", s1%value()

    s1 = s1 + '!'
    !print *, (s1 + '!')%value()
    print *, "Expected: Hello, world!!"
    print *, "Actual:   ", s1%value()

    print *, "Expected: -9876x"
    print *, "Actual:   ", new_string("-") + new_string("9876x")

    print *, "Expected: ell"
    print *, "Actual:   ", "Hello, world!!"(2:4)
    print *, "Expected: ell"
    print *, "Actual:   ", s1%substring(2, 4)

    print *, "Expected: He"
    print *, "Actual:   ", s1%substring(0, 2)

    print *, "Expected: "
    print *, "Actual:   ", "Hello, world!!"(5:1)
    print *, "Expected: "
    print *, "Actual:   ", s1%substring(5, 1)

    print *, "Expected: e"
    print *, "Actual:   ", s1%char_at(2)

    print *, "Expected: ", "Hello, world!!"
    print *, "Actual:   ", s1%map(identity)

    print *, "Expected: ", "aaaaaaaaaaaaaa"
    print *, "Actual:   ", s1%map(always_a)

    print *, "Expected: HELLO, WORLD!!"
    print *, "Actual:   ", s1%to_uppercase()

    print *, "Expected: hello, world!!"
    print *, "Actual:   ", s1%to_lowercase()

    print *, "Expected: Hello, world!!"
    print *, "Actual:   ", s1

    print *, "Expected: 0"
    print *, "Actual:   ", new_string(0)

    print *, "Expected: 5"
    print *, "Actual:   ", new_string(5)

    print *, "Expected: 12345"
    print *, "Actual:   ", new_string(12345)

    print *, "Expected: -8"
    print *, "Actual:   ", new_string(-8)

    print *, "Expected: -98765"
    print *, "Actual:   ", new_string(-98765)

    print *, "Expected: .true."
    print *, "Actual:   ", new_string(.true.)

    print *, "Expected: .false."
    print *, "Actual:   ", new_string(.false.)

    print *, "Expected:  T"
    print *, "Actual:   ", new_string(.false.) == new_string(".false.")

    print *, "Expected:  T"
    print *, "Actual:   ", new_string("") == new_string("")

    print *, "Expected:  F"
    print *, "Actual:   ", new_string("") == new_string("true")

    print *, "Expected:  F"
    print *, "Actual:   ", new_string("true") == new_string("")

    print *, "Expected:  F"
    print *, "Actual:   ", new_string("true") == new_string("truE")

    print *, "Expected:  T"
    print *, "Actual:   ", new_string(.false.) .eqic. new_string(".false.")

    print *, "Expected:  T"
    print *, "Actual:   ", new_string("") .eqic. new_string("")

    print *, "Expected:  F"
    print *, "Actual:   ", new_string("") .eqic. new_string("true")

    print *, "Expected:  F"
    print *, "Actual:   ", new_string("true") .eqic. new_string("")

    print *, "Expected:  T"
    print *, "Actual:   ", new_string("true") .eqic. new_string("truE")

    print *, "Expected:  T"
    print *, "Actual:   ", new_string("tRUe") .eqic. new_string("TrUe")

    print *, "Done"
end program string_test
