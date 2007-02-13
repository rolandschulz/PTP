module note_8_6

contains
  integer function signum(n)
    select case(n)
    case(:-1)
       signum = -1
    case(0)
       signum = 0
    case(1:)
       signum = 1
    end select
    
  end function signum

end module note_8_6

