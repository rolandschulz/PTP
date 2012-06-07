
/**
 * Sample OpenSHMEM code for PLDT testing
 */#include <stdlib.h>
#include <stdio.h>

#include "shmem.h"

static int swap_int;
static int add_int;
static int fetchAdd_int;

static long gLock;

int main (int argc, char* argv[])
{
    int total_num_tasks = -1;
    int this_task = -1;

    start_pes(0);

    total_num_tasks = _num_pes();

    if (total_num_tasks <= 0) {
        printf("FAILED\n");
        exit(1);
    } else {
        printf("There are  %d processing elements\n", total_num_tasks);
    }

    if (total_num_tasks < 2 || total_num_tasks % 2) {
        printf("FAILED: Number of processing elements should be an even number (at least 2)\n");
        exit(1);
    }

    this_task = _my_pe();

    if (this_task < 0){
        printf("FAILED\n");
        exit(1);
    } else {
        printf("This PE id is %d\n", this_task);
    }

    swap_int = -1;
    shmem_barrier_all();

    printf("Do some conditional swapping ...\n");

    if (this_task != 0) {
        int prev = shmem_int_cswap(&swap_int, -1, this_task, 0);
        if (prev == -1) {
            printf("pe %d was the winner\n", this_task);
        } else {
            printf("pe %d was a loser\n", this_task);
        }
    }

    add_int = 0;
    shmem_barrier_all();

    printf("Do some operations for remote add ...\n");
    shmem_int_add(&add_int, 1, 0);

    fetchAdd_int = 0;
    shmem_barrier_all();
    if (this_task == 0) {
        printf("initial value was 0, current value is %d after %d remote add operations\n", 
            add_int, total_num_tasks);
    }

    printf("Do some remote fetch and add\n");
    int prev = shmem_int_fadd(&fetchAdd_int, 1, 0);
    printf("before fetch and add the previous value is %d\n", prev);

    shmem_barrier_all();

    printf("Do some lock operations remotely ...\n");
    shmem_set_lock(&gLock);
    printf("Processing element %d got the lock\n", this_task);
    shmem_clear_lock(&gLock);

    shmem_barrier_all();
    
    return 0;
}

