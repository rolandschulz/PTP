/*
 * Copyright (c) 2009 National Center for Supercomputing Applications
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
#ifndef PTP_PBS_JOB_H_
#define PTP_PBS_JOB_H_

#include <stdbool.h>
#include "pbs_ifl.h"
#include "hash.h"

#define RSRC_arch	"arch"
#define RSRC_cput	"cput"
#define RSRC_file	"file"
#define RSRC_host	"host"
#define RSRC_mem	"mem"
#define RSRC_mpiprocs "mpiprocs"
#define RSRC_ncpus	"ncpus"
#define RSRC_nice	"nice"
#define RSRC_nodes	"nodes"
#define RSRC_nodect	"nodect"
#define RSRC_ompthreads "ompthreads"
#define RSRC_pcput	"pcput"
#define RSRC_pmem	"mem"
#define RSRC_pvmem	"pvmem"
#define RSRC_resc	"resc"
#define RSRC_vmem	"vmem"
#define RSRC_walltime	"walltime"
#define RSRC_mppe	"mppe"
#define RSRC_mppt	"mppt"
#define RSRC_pf		"pf"
#define RSRC_pmppt	"pmppt"
#define RSRC_pncpus	"pncpus"
#define RSRC_ppf	"ppf"
#define RSRC_procs	"procs"
#define RSRC_psds	"psds"
#define RSRC_sds	"sds"

#define DEFAULT_HASH_SIZE 4091

typedef struct attrl attrl;

struct PbsAttrList
{
	Hash* attrTable;
	Hash* rsrcTable;
};
typedef struct PbsAttrList PbsAttrList;

PbsAttrList* create_pbs_attr_list();
void free_pbs_attr_list(PbsAttrList*);
char* serialize_pbs_attr_list(PbsAttrList*);

bool is_valid_pbs_attr(char*, PbsAttrList*);
bool is_valid_pbs_rsrc(char*, PbsAttrList*);

attrl* create_job_attr_entry(char*, attrl*, PbsAttrList*);
void free_attrl(attrl*);
void free_attrl_recur(attrl* job_attr);
bool parse_arg_into_attrl(char*, attrl*, PbsAttrList*);
void print_attrl(attrl*);

char *copy_string(char *str);
void HashPut(Hash*, char*, void*);

#endif /* PTP_PBS_JOB_H_ */
