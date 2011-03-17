module test_import
    implicit none

    use something, only: method_a

    abstract interface
        function interface_record(this) result(status)
            import
            logical :: status
            class(method_a), target, intent(INOUT) :: this
        end function
    end interface

end module test_import
