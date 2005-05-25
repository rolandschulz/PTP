#include "orte_config.h"
#include <stdbool.h>

#include "include/orte_constants.h"
#include "mca/errmgr/errmgr.h"
#include "runtime/runtime.h"
#include "mca/gpr/gpr.h"

#include <mpi.h>

int main(int argc, char **argv)
{
        int rc, rank;

        MPI_Init(&argc, &argv);
        printf("Hello, World\n");

#if 0
        /* setup the runtime environment */
        if (ORTE_SUCCESS != (rc = orte_init())) {
            ORTE_ERROR_LOG(rc);
            return rc;
        }
        printf("Registry initted.\n");

        MPI_Comm_rank(MPI_COMM_WORLD, &rank);
 
        if(rank == 0) {
            printf("dumping registry . . .\n");
            printf("--------------------------------------------\n");
            orte_gpr.dump_segments(0);
            printf("--------------------------------------------\n");
        }

        /* finalize things */
        orte_finalize();

        printf("Registry finalized.\n");
#endif

	printf("Pausing for 10 seconds . . .\n");
	fflush(stdout);
	sleep(10);

        MPI_Finalize();

        return(0);
}

