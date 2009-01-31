! This is syntactically correct but semantically incorrect and meaningless
program Fortran2003
    import x, y
    import :: z

    type, bind(c) :: class
        private
        procedure(), public :: proc_component
    contains
        procedure                     type_bound_proc1
        procedure, non_overridable :: type_bound_proc3
        procedure                     type_bound_proc4 => proc
        procedure, non_overridable :: type_bound_proc6 => proc
    end type

    enum, bind(c)
        enumerator    apple = 3, orange, pear
        enumerator :: grapefruit
    end enum

    type(class), asynchronous, bind(c, name='x'), protected, value, volatile, pointer :: t1
    class(class) :: t2
    class(*)     :: t3

    common /c1/ t1, t2

    asynchronous      x1, x2
    asynchronous   :: x3
    bind(c)           x4
    bind(c,name=x) :: x5, /c1/
    protected         x6
    protected      :: x7, x8

    abstract interface
        subroutine s
        end subroutine s
    end interface

    procedure() p1
    procedure(integer) p2
    procedure(), pointer :: p3
    procedure(integer), save, optional :: p4, p5 => null(), p6

    ! EXECUTION PART

    associate (this => 3 + 4, that => 5)
        print *, this
        print *, that
    end associate

    select type (renamed => t3)
        type is (class)
            print *, "1"
        class is (class)
            print *, '2'
        class default
            print *, '3'
            if (3 .eq. 4) then
                print *, '?!'
            end if
            do i = 6,8
                print *, ":-)"
            end do
            do 51 i = 6,8
51              print *, ":-)"
    end select

!    if (t1%type_bound_proc1() .eq. 3) print *, "!"

    call t2%type_bound_proc4

    open (3,action=something,asynchronous='?',decimal='?',encoding='?',iomsg=v,round='?',sign='?')
    decimal=3
    wait (3,id=n,iostat=m)
    close (unit=3,iomsg=v)

    use, intrinsic :: some_module
    use :: some_other_module, operator(.x.) => operator(.y.)
    use :: some_third_module, only: operator(.x.) => operator(.y.)
end program
