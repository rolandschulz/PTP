program test
    use module, only: i, helper
    use nonexistant, only: what !<<<<< 3, 9, 3, 20, fail-initial
    implicit none
end program test
