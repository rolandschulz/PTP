program MyProgram
!implicit none

use module1, renamed_in_1 => rename_this
use module2, only: say_konnichiwa, renamed_in_2 => rename_this

call start

call say_hi
call say_bye
call say_konnichiwa
!call say_sayonara
call renamed_in_1
call renamed_in_2

stop

contains

subroutine start
  implicit none
  integer :: i
    print *, i
end subroutine

end
