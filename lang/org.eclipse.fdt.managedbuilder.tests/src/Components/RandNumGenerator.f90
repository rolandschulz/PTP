module RandNumGenerator

  interface getRandomNumber
    module procedure getRandomNumber_rand
  end interface getRandomNumber

contains

function getRandomNumber_rand()
  implicit none
  real(kind(1.0D0)) :: getRandomNumber_rand
  call random_number(getRandomNumber_rand)
end function getRandomNumber_rand

end module RandNumGenerator
