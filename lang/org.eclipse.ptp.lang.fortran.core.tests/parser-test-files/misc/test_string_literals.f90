! this module needs to be added to!!!
module test_string_literals
  implicit none
contains

  subroutine test_0()
    character, parameter :: a = 'don''t'
    character, parameter :: b = 'don""""''t'
    character, parameter :: c = 'i''like''f''o''rtra''''n'
    character, parameter :: d = "another""test"
    character, parameter :: e = "another"" """"test"
  end subroutine test_0
end module test_string_literals
