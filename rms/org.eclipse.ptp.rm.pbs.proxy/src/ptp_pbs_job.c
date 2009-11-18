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
#include <pbs_ifl.h>

PbsAttrList*
create_pbs_attr_list()
{
	void* element;
	PbsAttrList* pbs_attr_list = (PbsAttrList*) malloc(sizeof(PbsAttrList));
	pbs_attr_list->attrTable = HashCreate(DEFAULT_HASH_SIZE);
	pbs_attr_list->rsrcTable = HashCreate(DEFAULT_HASH_SIZE);
	HashPut(pbs_attr_list->attrTable, ATTR_a, ATTR_a);
	HashPut(pbs_attr_list->attrTable, ATTR_c, ATTR_c);
	HashPut(pbs_attr_list->attrTable, ATTR_e, ATTR_e);
	HashPut(pbs_attr_list->attrTable, ATTR_g, ATTR_g);
	HashPut(pbs_attr_list->attrTable, ATTR_h, ATTR_h);
	HashPut(pbs_attr_list->attrTable, ATTR_j, ATTR_j);
	HashPut(pbs_attr_list->attrTable, ATTR_J, ATTR_J);
	HashPut(pbs_attr_list->attrTable, ATTR_k, ATTR_k);
	HashPut(pbs_attr_list->attrTable, ATTR_l, ATTR_l);
	HashPut(pbs_attr_list->attrTable, ATTR_m, ATTR_m);
	HashPut(pbs_attr_list->attrTable, ATTR_o, ATTR_o);
	HashPut(pbs_attr_list->attrTable, ATTR_p, ATTR_p);
	HashPut(pbs_attr_list->attrTable, ATTR_q, ATTR_q);
	HashPut(pbs_attr_list->attrTable, ATTR_r, ATTR_r);
	HashPut(pbs_attr_list->attrTable, ATTR_u, ATTR_u);
	HashPut(pbs_attr_list->attrTable, ATTR_v, ATTR_v);
	HashPut(pbs_attr_list->attrTable, ATTR_A, ATTR_A);
	HashPut(pbs_attr_list->attrTable, ATTR_M, ATTR_M);
	HashPut(pbs_attr_list->attrTable, ATTR_N, ATTR_N);
	HashPut(pbs_attr_list->attrTable, ATTR_S, ATTR_S);
	HashPut(pbs_attr_list->attrTable, ATTR_depend, ATTR_depend);
	HashPut(pbs_attr_list->attrTable, ATTR_inter, ATTR_inter);
	HashPut(pbs_attr_list->attrTable, ATTR_sandbox, ATTR_sandbox);
	HashPut(pbs_attr_list->attrTable, ATTR_stagein, ATTR_stagein);
	HashPut(pbs_attr_list->attrTable, ATTR_stageout, ATTR_stageout);
	HashPut(pbs_attr_list->attrTable, ATTR_resvTag, ATTR_resvTag);
	HashPut(pbs_attr_list->attrTable, ATTR_resvID, ATTR_resvID);
	HashPut(pbs_attr_list->attrTable, ATTR_resv_start, ATTR_resv_start);
	HashPut(pbs_attr_list->attrTable, ATTR_resv_end, ATTR_resv_end);
	HashPut(pbs_attr_list->attrTable, ATTR_resv_duration, ATTR_resv_duration);
	HashPut(pbs_attr_list->attrTable, ATTR_auth_u, ATTR_auth_u);
	HashPut(pbs_attr_list->attrTable, ATTR_auth_g, ATTR_auth_g);
	HashPut(pbs_attr_list->attrTable, ATTR_auth_h, ATTR_auth_h);
	HashPut(pbs_attr_list->attrTable, ATTR_pwd, ATTR_pwd);
	HashPut(pbs_attr_list->attrTable, ATTR_cred, ATTR_cred);
	HashPut(pbs_attr_list->attrTable, ATTR_nodemux, ATTR_nodemux);
	HashPut(pbs_attr_list->attrTable, ATTR_umask, ATTR_umask);
	HashPut(pbs_attr_list->attrTable, ATTR_block, ATTR_block);
	HashPut(pbs_attr_list->attrTable, ATTR_convert, ATTR_convert);
	HashPut(pbs_attr_list->attrTable, ATTR_DefaultChunk, ATTR_DefaultChunk);
	HashPut(pbs_attr_list->rsrcTable, RSRC_arch, RSRC_arch);
	HashPut(pbs_attr_list->rsrcTable, RSRC_cput, RSRC_cput);
	HashPut(pbs_attr_list->rsrcTable, RSRC_file, RSRC_file);
	HashPut(pbs_attr_list->rsrcTable, RSRC_host, RSRC_host);
	HashPut(pbs_attr_list->rsrcTable, RSRC_mem, RSRC_mem);
	HashPut(pbs_attr_list->rsrcTable, RSRC_mpiprocs, RSRC_mpiprocs);
	HashPut(pbs_attr_list->rsrcTable, RSRC_ncpus, RSRC_ncpus);
	HashPut(pbs_attr_list->rsrcTable, RSRC_nice, RSRC_nice);
	HashPut(pbs_attr_list->rsrcTable, RSRC_nodes, RSRC_nodes);
	HashPut(pbs_attr_list->rsrcTable, RSRC_nodect, RSRC_nodect);
	HashPut(pbs_attr_list->rsrcTable, RSRC_ompthreads, RSRC_ompthreads);
	HashPut(pbs_attr_list->rsrcTable, RSRC_pcput, RSRC_pcput);
	HashPut(pbs_attr_list->rsrcTable, RSRC_pmem, RSRC_pmem);
	HashPut(pbs_attr_list->rsrcTable, RSRC_pvmem, RSRC_pvmem);
	HashPut(pbs_attr_list->rsrcTable, RSRC_resc, RSRC_resc);
	HashPut(pbs_attr_list->rsrcTable, RSRC_vmem, RSRC_vmem);
	HashPut(pbs_attr_list->rsrcTable, RSRC_walltime, RSRC_walltime);
	HashPut(pbs_attr_list->rsrcTable, RSRC_mppe, RSRC_mppe);
	HashPut(pbs_attr_list->rsrcTable, RSRC_mppt, RSRC_mppt);
	HashPut(pbs_attr_list->rsrcTable, RSRC_pf, RSRC_pf);
	HashPut(pbs_attr_list->rsrcTable, RSRC_pmppt, RSRC_pmppt);
	HashPut(pbs_attr_list->rsrcTable, RSRC_pncpus, RSRC_pncpus);
	HashPut(pbs_attr_list->rsrcTable, RSRC_ppf, RSRC_ppf);
	HashPut(pbs_attr_list->rsrcTable, RSRC_procs, RSRC_procs);
	HashPut(pbs_attr_list->rsrcTable, RSRC_psds, RSRC_psds);
	HashPut(pbs_attr_list->rsrcTable, RSRC_sds, RSRC_sds);
	return pbs_attr_list;
}

void
free_pbs_attr_list
(PbsAttrList* pbs_attr_list)
{
	if ( pbs_attr_list != NULL ) {
		HashDestroy(pbs_attr_list->attrTable, free);
		HashDestroy(pbs_attr_list->rsrcTable, free);
		free(pbs_attr_list);
	}
}

bool
is_valid_pbs_attr
(char* key, PbsAttrList* pbs_attr_list)
{
	if ( HashFind(pbs_attr_list->attrTable, key) == NULL ) return false;
	return true;
}

bool
is_valid_pbs_rsrc
(char* key, PbsAttrList* pbs_attr_list)
{
	if ( HashFind(pbs_attr_list->rsrcTable, key) == NULL ) return false;
	return true;
}

char*
serialize_pbs_attr_list
(PbsAttrList* pbs_attr_list)
{
	char buffer[16384];
	HashEntry *entry;
	int i = sprintf(&buffer[0], "%s", "<pbs-job-attributes>");
	HashSet(pbs_attr_list->attrTable);
	while( (entry = HashGet(pbs_attr_list->attrTable)) != NULL ) {
		i += sprintf(&buffer[i], "<attr name=\"%s\"/>", (char*)entry->h_data);
	}
	HashSet(pbs_attr_list->rsrcTable);
	while( (entry = HashGet(pbs_attr_list->rsrcTable)) != NULL ) {
		i += sprintf(&buffer[i], "<rsrc name=\"%s\"/>", (char*)entry->h_data);
	}
	sprintf(&buffer[i], "%s", "</pbs-job-attributes>");
	return copy_string(buffer);
}

attrl*
create_job_attr_entry
( char* arg, attrl* last, PbsAttrList* pbs_attr_lst)
{
	attrl* job_attr = (attrl *) malloc(sizeof(attrl));
	if ( parse_arg_into_attrl(arg, job_attr, pbs_attr_lst) == false ) return NULL;
	if ( last != NULL ) last->next = job_attr;
	return job_attr;
}

bool
parse_arg_into_attrl
( char* arg, attrl* job_attr, PbsAttrList* pbs_attr_lst)
{
	char *part[3];
	char* prefix = NULL;
	part[1] = NULL;
	part[0] = strtok(arg, "=");
	part[2] = strtok(NULL, "=");
	prefix = strstr(part[0], "Resource");
	if (prefix != NULL) {
		part[0] = "Resource_List";
		strtok(prefix, "_");
		part[1] = strtok(NULL, "-");
	}
	if ( is_valid_pbs_attr(part[0], pbs_attr_lst) == false ) {
		return false;
	}
	job_attr->name = copy_string(part[0]);
	if ( part[1] != NULL && is_valid_pbs_rsrc(part[1], pbs_attr_lst) == false ) {
		return false;
	}
	job_attr->resource = copy_string(part[1]);
	job_attr->value = copy_string(part[2]);
	return true;
}

void
free_attrl
(attrl* job_attr)
{
	if (job_attr != NULL) {
		if ( job_attr->name != NULL ) free(job_attr->name);
		if ( job_attr->resource != NULL ) free(job_attr->resource);
		if ( job_attr->value != NULL ) free(job_attr->value);
		job_attr->next = NULL;
		free(job_attr);
	}
}

void
free_attrl_recur
(attrl* job_attr)
{
	if (job_attr != NULL) {
		if ( job_attr->name != NULL ) free(job_attr->name);
		if ( job_attr->resource != NULL ) free(job_attr->resource);
		if ( job_attr->value != NULL ) free(job_attr->value);
		if ( job_attr->next != NULL ) free_attrl_recur(job_attr->next);
		free(job_attr);
	}
}

void
print_attrl
(attrl* job_attr)
{
	if (job_attr != NULL) {
		printf("<job-attribute");
		if ( job_attr->name != NULL ) printf(" name=\"%s\"", job_attr->name);
		if ( job_attr->resource != NULL ) printf(" resource=\"%s\"", job_attr->resource);
		if ( job_attr->value != NULL ) printf(" value=\"%s\"", job_attr->value);
		if ( job_attr->next != NULL) {
			if ( job_attr->next->resource != NULL )
				printf(" next=\"Resource_List_%s\"", job_attr->next->resource);
			else
				printf(" next=\"%s\"", job_attr->next->name);
		}
		printf( "/>\n");
	}
}

void HashPut
(Hash* table, char* key, void* value)
{
	HashInsert(table, HashCompute(key, strlen(key)), copy_string(value));
}

char *
copy_string
(char *str)
{
	if (str != NULL) {
		int 	len = strlen(str);
		char *	res = (char *)malloc(len+1);
		memcpy(res, str, len);
		*(res+len) = '\0';
		return res;
	}
	return NULL;
}
