! Demonstrates a parser bug in NAG Fortran Compiler Release 5.2(649)
! NPLUX52NA for x86-32 Linux
!
! Line 34 reports a syntax error due to the empty parentheses ()
! However, the statement should parse with
!   pointer-assignment-stmt (R735)
!     > data-target (R739)
!     > expr (R722)
!     > ...
!     > primary (R701)
!     > function-reference (R1217)
!     > procedure-designator  "("  [actual-arg-spec-list]  ")"
! where actual-arg-spec-list is marked as being optional and
! procedure-designator (R1219) > data-ref "%" binding-name
!
! J. Overbey 2/23/09 - overbey2@illinois.edu
module m
    type t; contains; procedure :: ptr; end type
contains
    function ptr(self)
        class(t), intent(in) :: self
        class(*), pointer :: ptr
        ptr => null()
    end function
end module

program program
    use m

    class(t), pointer :: t_ptr
    class(*), pointer :: any_ptr

    allocate (t_ptr)
    any_ptr => t_ptr%ptr()  ! <<<<<<<<<< Reported syntax error
end program
