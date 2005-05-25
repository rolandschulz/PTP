#include "orte_config.h"
#include <stdbool.h>

#include "include/orte_constants.h"
#include "mca/errmgr/errmgr.h"
#include "runtime/runtime.h"
#include "mca/gpr/gpr.h"

int main(int argc, char **argv)
{
	int rc, rank;
	char junk[16];

	/* setup the runtime environment */
	if (ORTE_SUCCESS != (rc = orte_init())) {
	    ORTE_ERROR_LOG(rc);
	    return rc;
	}

	printf("Registry initted.\n");

	printf("dumping registry .  .  .\n");

	printf("----------------------------------------------------\n");
	orte_gpr.dump_segments(0);
	printf("----------------------------------------------------\n");

	orte_finalize();

	printf("Registry finalized.\n");

	return(0);
}
