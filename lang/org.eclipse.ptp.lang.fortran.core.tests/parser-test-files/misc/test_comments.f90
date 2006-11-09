! start of the module
module test_comments

contains
  subroutine test_0()
    ! this is here just to make sure parser will remove it
    integer :: a 
    integer :: b ! this still should make the parser return a T_EOS
    a = b
  end subroutine test_0
! end of the contained procedures
end module test_comments! end of the module
