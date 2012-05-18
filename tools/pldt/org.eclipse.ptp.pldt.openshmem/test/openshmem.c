/*
 * Sample OpenSHMEM program, from the OpenSHMEM specification
 */
#include <stdio..h>

#include <shmem.h>
int main(int argc, char* argv[]) {
	int me, my_num_pes;
	/*
	 ** Starts/Initializes SHMEM/OpenSHMEM
	 */
	start_pes(0);
	/*
	 ** Fetch the number or processes
	 ** Some implementations use num_pes();
	 */
	my_num_pes = _num_pes();
	/*
	 ** Assign my process ID to me
	 */
	me = _my_pe();
	printf("Hello World from %d of %d\n", me, my_num_pes);
	return 0;
}

/**
 * Uses more APIs
 */
int aaa, bbb;
int sample(int argc, char * argv[]) {
	start_pes(0);
	shmem_int_get(&aaa, &bbb, 1, (_my_pe() + 1) % _num_pes());
	shmem_barrier_all();
}
