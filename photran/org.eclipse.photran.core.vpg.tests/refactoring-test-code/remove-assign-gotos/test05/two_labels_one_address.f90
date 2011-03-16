! Test 5: 2 ASSIGN Labels, One GOTO address each
! Test passes and replaces two assigned gotos and creates two select case statements
program two_labels_one_address !<<<<< 1, 1, 22, 12, true, pass
    real :: area
    real :: radius

    assign 100 to label1
    goto 7000

100 radius = 3.0
    assign 325 to label2
    goto 9000

325 stop

7000 print *, "hello"
    goto label1

9000 area = 3.1415 * r**2
    goto label2

end program
