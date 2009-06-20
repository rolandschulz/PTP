module module
  interface module_overload !2,13
    subroutine overload_int(value) !3,16
      integer, intent(in) :: value
    end subroutine overload_int !5,20
    module procedure overload_char !6,22
  end interface module_overload !7,17
contains
  subroutine overload_char(value) !9,14
    character, intent(in) :: value
    print *, "(Module) Character", value
  end subroutine overload_char !12,18
end module module

subroutine overload_int(value) !15,12
  integer, intent(in) :: value
  print *, "Integer", value
end subroutine overload_int !18,16

program test
  interface overload
    subroutine overload_int(value) !22,16
      integer, intent(in) :: value
    end subroutine overload_int !24,20

    subroutine overload_char(value) !26,16
      character, intent(in) :: value
    end subroutine overload_char !28,20
  end interface overload !29,17

  call overload(1)        !31,8
  call overload('c')      !32,8
  call overload_int(1)    !33,8
  call overload_char('c') !34,8
  call call_module
contains
  subroutine call_module
    use module
    call module_overload(2)   !39,10
    call module_overload('d') !40,10
    call overload_int(2)      !41,10
    call overload_char('d')   !42,10
  end subroutine call_module
end program

subroutine overload_char(value) !46,12
  character, intent(in) :: value
  print *, "Character", value
end subroutine overload_char !49,16
