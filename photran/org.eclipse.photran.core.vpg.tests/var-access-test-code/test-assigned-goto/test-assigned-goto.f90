program testassignedgoto
    integer :: return_label

    assign 200 to return_label
    !<<<<< 4,19,WRITE

    goto return_label
    !<<<<< 7,10,READ

    print *, "I will never be printed..."

200 print *, "but I will!"

end program testassignedgoto
