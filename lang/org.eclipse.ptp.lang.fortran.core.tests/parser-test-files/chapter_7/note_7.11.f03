! created variables, etc., for each of the initialization expressions
integer, parameter :: a = 3
integer, parameter :: b = -3+4
character(len=*), parameter :: c = 'ab'
character(len=*), parameter :: d = 'ab' // 'cd'
character(len=*), parameter :: e = ('ab' // 'cd') // 'ef'
integer :: my_array(10)
integer, parameter :: f = size(my_array)
integer, parameter :: g = digits(a) + 4
real, parameter :: h = 4.0 * atan(1.0)
integer, parameter :: number_of_decimal_digits = 100
real :: i = ceiling(number_of_decimal_digits / log10(radix(0.0)))

end
