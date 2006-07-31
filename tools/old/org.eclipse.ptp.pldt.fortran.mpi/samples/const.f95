program main
    use MPIModule
    
    real :: buf(100)
    integer :: count, datatype, dest, tag, ierr
    integer(kind=MPI_COMM_KIND) :: comm
    
    double precision :: d1,d2 
    
    d1=3.1415926535897931
    d2=3.1415926535897931D0
    
    print *, sin(d1), sin(d2)
    
    call MPI_Send(buf, count, datatype, dest, tag, comm, ierr)
    call MPI_Recv(buf, count, datatype, dest, tag, comm, ierr)
    
end program
