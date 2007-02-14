class(*), allocatable :: new
class(*), pointer :: old

allocate(new, source=old) ! allocate NEW with the value and dynamic type of OLD

end
