program program                                                      ! LINE 1
    use module
    implicit none
    
    external declared_external_subroutine
    integer :: unused_local_variable
    integer :: assigned_local_variable
    integer :: local_variable_accessed_from_internal_subroutine
    
    ! Don't call unused_defined_external_subroutine
    call defined_external_subroutine
    call declared_external_subroutine
    call undeclared_external_subroutine
    call internal_subroutine
    call used_module_subroutine
    
    assigned_local_variable = local_variable_accessed_from_internal_subroutine
    
    print *, assigned_local_variable
    
contains

    subroutine internal_subroutine                                   ! LINE 23
        print *, "Called internal_subroutine"
        local_variable_accessed_from_internal_subroutine = 12345
    end subroutine internal_subroutine

    subroutine unused_internal_subroutine
        print *, "Called unused_internal_subroutine"
    end subroutine internal_subroutine

end program program

subroutine defined_external_subroutine
    implicit none
    print *, "Called defined_external_subroutine"
end subroutine defined_external_subroutine

subroutine unused_defined_external_subroutine
    implicit none
    print *, "Called unused_defined_external_subroutine"
end subroutine unused_defined_external_subroutine