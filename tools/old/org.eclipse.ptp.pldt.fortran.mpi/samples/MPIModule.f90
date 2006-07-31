module MPIModule

  integer, parameter :: MPI_COMM_KIND = 4

contains

  subroutine MPI_Send(buf, count, datatype, dest, tag, comm, ierr)
    real :: buf(*)
    integer :: count, datatype, dest, tag, ierr
    integer(kind=MPI_COMM_KIND) :: comm
  end subroutine MPI_Send
  
  
  subroutine MPI_Recv(buf, count, datatype, dest, tag, comm, ierr)
    real :: buf(*)
    integer :: count, datatype, dest, tag, ierr
    integer(kind=MPI_COMM_KIND) :: comm
  end subroutine MPI_Send

end module MPIModule
