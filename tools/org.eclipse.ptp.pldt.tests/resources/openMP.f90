! This is a sample OpenMP program  
program OpenMP                                     
    integer :: num_threads, id                     
    !$omp parallel private(num_threads, id)        
    id = omp_get_thread_num()
    print *, 'This is thread ', id
     if (id == 0) then
      num_threads = omp_get_num_threads()
      print *, 'Total threads: ', num_threads
    end if
    !$omp end parallel
end program 