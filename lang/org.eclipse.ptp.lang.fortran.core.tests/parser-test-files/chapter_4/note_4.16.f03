!         NOTE 4.16

!         The ultimate components of objects of the derived type kids defined below are name, age, and
!         other_kids.

         type :: person
           character(len=20) :: name
           integer :: age
         end type person

         type :: kids
           type(person) :: oldest_child
           type(person), allocatable, dimension(:) :: other_kids
         end type kids
end