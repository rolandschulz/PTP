module module                                                        ! LINE 1
    implicit none
    
    private :: private_module_subroutine
    
contains
    
    subroutine used_module_subroutine
        print *, "Called used_module_subroutine"
    end subroutine used_module_subroutine
    
    subroutine unused_module_subroutine
        print *, "Called unused_module_subroutine"
    end subroutine unused_module_subroutine
    
    subroutine private_module_subroutine
        print *, "Called private_module_subroutine"
    end subroutine private_module_subroutine
    
end module module
