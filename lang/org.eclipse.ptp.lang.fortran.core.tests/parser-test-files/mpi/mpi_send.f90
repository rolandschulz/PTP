module MPI

  interface
    subroutine mpi_send(array, dest, tag, comm, error) BIND(C)
      class(*), dimension(:) :: array
      integer, optional :: dest, tag, error
      type(MPI_Comm), optional :: comm
    end subroutine mpi_send
  end interface
  
contains

  subroutine mpi_send(array, dest, tag, comm, error) BIND(C)
    class(*), dimension(:) :: array
    integer, optional :: dest, tag, error
    type(MPI_Comm), optional :: comm
    
    select type(array)
      class is(integer)
        return
    end select
    
  end subroutine mpi_send


end module MPI