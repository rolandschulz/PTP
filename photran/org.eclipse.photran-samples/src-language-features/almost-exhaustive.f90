program Main
    integer, parameter :: k6=selected_int_kind(6) ! Kind for range [-999999,999999]
    integer, parameter :: n1 = -123456_k6
    integer, parameter :: n2 = -123456_4
    integer, parameter :: long = selected_real_kind(9, 99) ! 9 sig decimals, exponent
                                                           ! range 10^-99 to 10^99
    integer, parameter :: asci = kind('ASCII')
    integer(kind=long) :: a
    character(len=20, kind=1) :: english_word
    character(20) :: length_twenty
    
    type person
        character(10) :: name
        real          :: age
        integer       :: id
    end type
    type(person) :: me = person('Jeff', 23, 12345)
    type(person) :: you
        
    real, dimension(10) :: array1thru10
    real, dimension(-10,5) :: arrayneg10thru5
    real, dimension(-10:5, -20:-1, 2) :: threedarray
    real, dimension(5) :: arrayconst = (/ 1, 2, 3, 4, 5 /)
    
    character(len=10) :: ten = "1234567890"
    
    real, pointer :: realptr => null()
        
    you%name = 'Bob'
    you%age  = 17.25
    you%id   = 18
        
    print *, "Kinds:", kind(k6), kind(n1), kind(n2), kind(1.0)
    print *, "Precision:", precision(1.0_long) ! will be at least 9
    print *, "Num decimal digits supported:", range(2_k6)
    print *, "Num decimal digits supported:", range(1.0_long) ! will be at least 99
    !ERROR!print *, "Bin/octal/hex:", b'01100110', o'076543', z'10fa'
    print *, "Real literal constant:", -10.6e-11, 1., -0.1, 1e-1, 3.141592653
    print *, "Complex:", (1., 3.2), (1, .99e-2), (1.0, 3.7_8)
    print *, 'He said "Hello"', "This contains an '", 'Isn''t it a nice day'
    print *, 'This is a long string     &
            & which spans several lines &
            & unnecessarily.'
    !ERROR!print *, asci_"String"
    print *, .false._1, .true._long
    print *, ten(:5), ten(6:), ten(3:7), ten(:), you%name(1:2), 'Hello'(:2)
end program
