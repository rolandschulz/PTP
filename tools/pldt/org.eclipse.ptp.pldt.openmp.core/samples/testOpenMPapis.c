/**
 * This is not a syntactically correct file, but is used to test hover help
 * on each of the APIs.
 */
#include "omp.h"


int main(int argc, char* argv[]){
	omp_destroy_lock();
	omp_destroy_nest_lock();
	omp_get_active_level();
	omp_get_ancestor_thread_num();
	omp_get_dynamic();
	omp_in_final();
	omp_get_level();
	omp_get_max_active_levels();
	omp_get_max_threads();
	omp_get_nested();
	omp_get_num_procs();
	omp_get_num_threads();
	omp_get_schedule();
	omp_get_team_size();
	omp_get_thread_limit();
	omp_get_thread_num();
	omp_get_wtick();
	omp_get_wtime();
	omp_in_parallel();
	omp_init_lock();
	omp_init_nest_lock();
	omp_set_dynamic();
	omp_set_lock();
	omp_set_nest_lock();
	omp_set_nested();
	omp_set_num_threads();
	omp_set_max_active_levels();
	omp_set_schedule();
	omp_test_lock();
	omp_test_nest_lock();
	omp_unset_lock();
	omp_unset_nest_lock();
}

