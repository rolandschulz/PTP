! Test 6: 2 ASSIGN Labels, Three GOTO address each
! Test passes and creates two select cases and replaces the assign statements
program two_labels_one_address !<<<<< 1, 1, 38, 12, true, pass
    real :: area
    real :: radius

    assign 20 to label1
    goto 7000

10  assign 30 to label1
    goto 7000

20  assign 10 to label1
    goto 7000
    stop


30  radius = 3.0
    assign 200 to label2
    goto 9000

200 radius = 4.0
    assign 300 to label2
    goto 9000

300 radius = 5.0
    assign 325 to label2
    goto 9000

325 stop

7000 print *, "hello"
    goto label1

9000 area = 3.1415 * r**2
    goto label2

end program
