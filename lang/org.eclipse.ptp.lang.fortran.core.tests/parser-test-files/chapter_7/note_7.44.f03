program note_7_44
real, target :: mydata ( nr*nc )  ! an automatic array
real, pointer :: matrix (:,:)     ! a rank-two view of mydata
real, pointer :: view_diag(:)
matrix(1:nr,1:nc) => mydata       ! the matrix view of the data
view_diag => mydata(1::nr+1)      ! the diagonal of matrix

end program note_7_44
