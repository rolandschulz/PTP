program p
    call ctn1
    call ctn2
    call ext1
    call ext2
contains
  subroutine ctn1
    call ctn2
    call ext1
    call ext2
  end subroutine
  
  subroutine ctn2
    call ctn1
    call ext1
    call ext2
  end subroutine
end program p

subroutine ext1
    call ext2
end subroutine ext1

subroutine ext2
    call ext1
end subroutine ext2
