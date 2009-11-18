/*
 * Copyright (c) 2009 National Center for Supercomputing Applications
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include <stdio.h>
#include <ptp_pbs_job.h>
#include "hash.h"
#include "list.h"
#include <pbs_ifl.h>

int
main()
{
	int i;
	int argc = 7;
	attrl *attributes[7];
	char argv[7][32];
	char* serial;
	PbsAttrList* aList;
	attrl* last = NULL;

	sprintf( argv[0], "%s", "Job_Name=testPBSjob");
	sprintf( argv[1], "%s", "Account_Name=fyl");
	sprintf( argv[2], "%s", "Phoney_argument=foo");
	sprintf( argv[3], "%s", "Error_Path=/tmp/err.log");
	sprintf( argv[4], "%s", "Output_Path=/tmp/out.log");
	sprintf( argv[5], "%s", "Resource_walltime=00:25");
	sprintf( argv[6], "%s", "Resource_ncpus=256");

	aList = create_pbs_attr_list();
	serial = serialize_pbs_attr_list(aList);
	printf( "%s\n", serial);
	free(serial);

	for (i = 0; i < argc; i++) {
		attributes[i] = create_job_attr_entry(argv[i], last, aList);
		if ( attributes[i] == NULL ) {
			fprintf(stderr, "bad argument %s\n", argv[i]);
			continue;
		}
		last = attributes[i];
	}

	for (i = 0; i < argc; i++) {
		print_attrl(attributes[i]);
	}

	free_attrl_recur(attributes[0]);
	free_pbs_attr_list(aList);
	return 0;
}
