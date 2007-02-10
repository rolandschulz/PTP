module c_assoc
  use, intrinsic :: iso_c_binding
  implicit none

contains
  subroutine test_c_assoc_0(my_c_ptr) bind(c)
    use, intrinsic :: iso_c_binding
    type(c_ptr), value :: my_c_ptr
    
    if(c_associated(my_c_ptr)) then
       print *, 'my_c_ptr is associated'
    else
       print *, 'my_c_ptr is NOT associated'
    endif
  end subroutine test_c_assoc_0

  subroutine test_c_assoc_1(my_c_ptr_1, my_c_ptr_2) bind(c)
    use, intrinsic :: iso_c_binding
    type(c_ptr), value :: my_c_ptr_1
    type(c_ptr), value :: my_c_ptr_2
    
    if(c_associated(my_c_ptr_1, my_c_ptr_2)) then
       print *, 'my_c_ptr_1 and my_c_ptr_2 are associated'
    else
       print *, 'my_c_ptr_1 and my_c_ptr_2 are NOT associated'
    endif
  end subroutine test_c_assoc_1
end module c_assoc
