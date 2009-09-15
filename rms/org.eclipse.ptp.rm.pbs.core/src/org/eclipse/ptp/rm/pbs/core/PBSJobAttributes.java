/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rm.pbs.core;

import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;


/**
 * Job attributes
 */
public class PBSJobAttributes {
	private static final String ACCOUNT_NAME_ATTR_ID = "Account_Name"; //$NON-NLS-1$
	private static final String CHECKPOINT_ATTR_ID = "Checkpoint"; //$NON-NLS-1$
	private static final String COMMENT_ATTR_ID = "comment"; //$NON-NLS-1$
	private static final String DEPEND_ATTR_ID = "depend"; //$NON-NLS-1$
	private static final String ERROR_PATH_ATTR_ID = "Error_Path"; //$NON-NLS-1$
	private static final String EXECUTION_TIME_ATTR_ID = "Execution_Time"; //$NON-NLS-1$
	private static final String GROUP_LIST_ATTR_ID = "group_list"; //$NON-NLS-1$
	private static final String HOLD_TYPES_ATTR_ID = "Hold_Types"; //$NON-NLS-1$
	private static final String JOB_NAME_ATTR_ID = "Job_Name"; //$NON-NLS-1$
	private static final String JOIN_PATH_ATTR_ID = "Join_Path"; //$NON-NLS-1$
	private static final String KEEP_FILES_ATTR_ID = "Keep_Files"; //$NON-NLS-1$
	private static final String MAIL_POINTS_ATTR_ID = "Mail_Points"; //$NON-NLS-1$
	private static final String MAIL_USERS_ATTR_ID = "Mail_Users"; //$NON-NLS-1$
	private static final String NO_STDIO_SOCKETS_ATTR_ID = "no_stdio_sockets"; //$NON-NLS-1$
	private static final String OUTPUT_PATH_ATTR_ID = "Output_Path"; //$NON-NLS-1$
	private static final String PRIORITY_ATTR_ID = "Priority"; //$NON-NLS-1$
	private static final String RERUNNABLE_ATTR_ID = "Rerunnable"; //$NON-NLS-1$
	private static final String RES_ARCH_ATTR_ID = "Resource_List.arch"; //$NON-NLS-1$
	private static final String RES_CPUT_ATTR_ID = "Resource_List.cput"; //$NON-NLS-1$
	private static final String RES_FILE_ATTR_ID = "Resource_List.file"; //$NON-NLS-1$
	private static final String RES_HOST_ATTR_ID = "Resource_List.host"; //$NON-NLS-1$
	private static final String RES_MEM_ATTR_ID = "Resource_List.mem"; //$NON-NLS-1$
	private static final String RES_MPIPROCS_ATTR_ID = "Resource_List.mpiprocs"; //$NON-NLS-1$
	private static final String RES_NCPUS_ATTR_ID = "Resource_List.ncpus"; //$NON-NLS-1$
	private static final String RES_NICE_ATTR_ID = "Resource_List.nice"; //$NON-NLS-1$
	private static final String RES_NODES_ATTR_ID = "Resource_List.nodes"; //$NON-NLS-1$
	private static final String RES_NODECT_ATTR_ID = "Resource_List.nodect"; //$NON-NLS-1$
	private static final String RES_OMPTHREADS_ATTR_ID = "Resource_List.ompthreads"; //$NON-NLS-1$
	private static final String RES_PCPUT_ATTR_ID = "Resource_List.pcput"; //$NON-NLS-1$
	private static final String RES_PMEM_ATTR_ID = "Resource_List.pmem"; //$NON-NLS-1$
	private static final String RES_PVMEM_ATTR_ID = "Resource_List.pvmem"; //$NON-NLS-1$
	private static final String RES_RESC_ATTR_ID = "Resource_List.resc"; //$NON-NLS-1$
	private static final String RES_VMEM_ATTR_ID = "Resource_List.vmem"; //$NON-NLS-1$
	private static final String RES_WALLTIME_ATTR_ID = "Resource_List.walltime"; //$NON-NLS-1$
	private static final String RES_MPPE_ATTR_ID = "Resource_List.mppe"; //$NON-NLS-1$
	private static final String RES_MPPT_ATTR_ID = "Resource_List.mppt"; //$NON-NLS-1$
	private static final String RES_PF_ATTR_ID = "Resource_List.pf"; //$NON-NLS-1$
	private static final String RES_PMPPT_ATTR_ID = "Resource_List.pmppt"; //$NON-NLS-1$
	private static final String RES_PNCPUS_ATTR_ID = "Resource_List.pncpus"; //$NON-NLS-1$
	private static final String RES_PPF_ATTR_ID = "Resource_List.ppf"; //$NON-NLS-1$
	private static final String RES_PROCS_ATTR_ID = "Resource_List.procs"; //$NON-NLS-1$
	private static final String RES_PSDS_ATTR_ID = "Resource_List.psds"; //$NON-NLS-1$
	private static final String RES_SDS_ATTR_ID = "Resource_List.sds"; //$NON-NLS-1$
	private static final String SHELL_PATH_LIST_ATTR_ID = "Shell_Path_List"; //$NON-NLS-1$
	private static final String STAGEIN_ATTR_ID = "stagein"; //$NON-NLS-1$
	private static final String STAGEOUT_ATTR_ID = "stageout"; //$NON-NLS-1$
	private static final String UMASK_ATTR_ID = "umask"; //$NON-NLS-1$
	private static final String USER_LIST_ATTR_ID = "User_List"; //$NON-NLS-1$
	private static final String VARIABLE_LIST_ATTR_ID = "Variable_List"; //$NON-NLS-1$

	private static final IAttributeDefinition<?,?,?>[] attrDefs = new IAttributeDefinition[]{
				new StringAttributeDefinition(ACCOUNT_NAME_ATTR_ID, ACCOUNT_NAME_ATTR_ID, "", true, ""),
				new StringAttributeDefinition(CHECKPOINT_ATTR_ID, CHECKPOINT_ATTR_ID, "", true, ""),
				new StringAttributeDefinition(COMMENT_ATTR_ID, COMMENT_ATTR_ID, "", true, ""),
				new StringAttributeDefinition(DEPEND_ATTR_ID, DEPEND_ATTR_ID, "", true, ""),
				new StringAttributeDefinition(ERROR_PATH_ATTR_ID, ERROR_PATH_ATTR_ID, "", true, ""),
				new StringAttributeDefinition(EXECUTION_TIME_ATTR_ID, EXECUTION_TIME_ATTR_ID, "", true, ""),
				new StringAttributeDefinition(GROUP_LIST_ATTR_ID, GROUP_LIST_ATTR_ID, "", true, ""),
				new StringAttributeDefinition(HOLD_TYPES_ATTR_ID, HOLD_TYPES_ATTR_ID, "", true, ""),
				new StringAttributeDefinition(JOB_NAME_ATTR_ID, JOB_NAME_ATTR_ID, "", true, ""),
				new StringAttributeDefinition(JOIN_PATH_ATTR_ID, JOIN_PATH_ATTR_ID, "", true, ""),
				new StringAttributeDefinition(KEEP_FILES_ATTR_ID, KEEP_FILES_ATTR_ID, "", true, ""),
				new StringAttributeDefinition(MAIL_POINTS_ATTR_ID, MAIL_POINTS_ATTR_ID, "", true, ""),
				new StringAttributeDefinition(MAIL_USERS_ATTR_ID, MAIL_USERS_ATTR_ID, "", true, ""),
				new StringAttributeDefinition(NO_STDIO_SOCKETS_ATTR_ID, NO_STDIO_SOCKETS_ATTR_ID, "", true, ""),
				new StringAttributeDefinition(OUTPUT_PATH_ATTR_ID, OUTPUT_PATH_ATTR_ID, "", true, ""),
				new IntegerAttributeDefinition(PRIORITY_ATTR_ID, PRIORITY_ATTR_ID, "", true, 0),
				new StringAttributeDefinition(RERUNNABLE_ATTR_ID, RERUNNABLE_ATTR_ID, "", true, ""),
				new StringAttributeDefinition(RES_ARCH_ATTR_ID, RES_ARCH_ATTR_ID, "System architecture", true, ""),
				new StringAttributeDefinition(RES_CPUT_ATTR_ID, RES_CPUT_ATTR_ID, "Maximum, aggregate CPU time required by all processes", true, ""),
				new StringAttributeDefinition(RES_FILE_ATTR_ID, RES_FILE_ATTR_ID, "Maximum disk space requirements for any single file to be created", true, ""),
				new StringAttributeDefinition(RES_HOST_ATTR_ID, RES_HOST_ATTR_ID, "Name of requested host/node", true, ""),
				new StringAttributeDefinition(RES_MEM_ATTR_ID, RES_MEM_ATTR_ID, "Maximum amount of physical memory (RAM)", true, ""),
				new IntegerAttributeDefinition(RES_MPIPROCS_ATTR_ID, RES_MPIPROCS_ATTR_ID, "Number of MPI processes for this chunk", true, 0),
				new IntegerAttributeDefinition(RES_NCPUS_ATTR_ID, RES_NCPUS_ATTR_ID, "Number of CPUs (processors)", true, 0),
				new IntegerAttributeDefinition(RES_NICE_ATTR_ID, RES_NICE_ATTR_ID, "Requested job priority", true, 0),
				new StringAttributeDefinition(RES_NODES_ATTR_ID, RES_NODES_ATTR_ID, "Number and/or type of nodes", true, ""),
				new IntegerAttributeDefinition(RES_NODECT_ATTR_ID, RES_NODECT_ATTR_ID, "Number of chunks in resource request from selection directive, or number of vnodes requested from node specification", true, 0),
				new IntegerAttributeDefinition(RES_OMPTHREADS_ATTR_ID, RES_OMPTHREADS_ATTR_ID, "Number of OpenMP threads for this chunk", true, 0),
				new StringAttributeDefinition(RES_PCPUT_ATTR_ID, RES_PCPUT_ATTR_ID, "Per-process maximum CPU time", true, ""),
				new StringAttributeDefinition(RES_PMEM_ATTR_ID, RES_PMEM_ATTR_ID, "Per-process maximum amount of physical memory", true, ""),
				new StringAttributeDefinition(RES_PVMEM_ATTR_ID, RES_PVMEM_ATTR_ID, "Per-process maximum amount of virtual memory", true, ""),
				new StringAttributeDefinition(RES_RESC_ATTR_ID, RES_RESC_ATTR_ID, "Single-node variable resource specification string", true, ""),
				new StringAttributeDefinition(RES_VMEM_ATTR_ID, RES_VMEM_ATTR_ID, "Maximum, aggregate amount of virtual memory used by all concurrent processes", true, ""),
				new StringAttributeDefinition(RES_WALLTIME_ATTR_ID, RES_WALLTIME_ATTR_ID, "Maximum amount of real time (wall-clock elapsed time)", true, ""),
				new IntegerAttributeDefinition(RES_MPPE_ATTR_ID, RES_MPPE_ATTR_ID, "Number of processing elements used by a single process", true, 0),
				new StringAttributeDefinition(RES_MPPT_ATTR_ID, RES_MPPT_ATTR_ID, "Maximum wallclock time used on the MPP", true, ""),
				new StringAttributeDefinition(RES_PF_ATTR_ID, RES_PF_ATTR_ID, "Maximum number of file system blocks that can be used by all process", true, ""),
				new StringAttributeDefinition(RES_PMPPT_ATTR_ID, RES_PMPPT_ATTR_ID, "Maximum amount of wall clock time used on the MPP by a single process", true, ""),
				new IntegerAttributeDefinition(RES_PNCPUS_ATTR_ID, RES_PNCPUS_ATTR_ID, "Maximum number of processors used by any single process", true, 0),
				new StringAttributeDefinition(RES_PPF_ATTR_ID, RES_PPF_ATTR_ID, "Maximum number of file system blocks that can be used by a single process", true, ""),
				new IntegerAttributeDefinition(RES_PROCS_ATTR_ID, RES_PROCS_ATTR_ID, "Maximum number of processes", true, 0),
				new StringAttributeDefinition(RES_PSDS_ATTR_ID, RES_PSDS_ATTR_ID, "Maximum number of data blocks on the SDS (secondary data storage) for any process", true, ""),
				new StringAttributeDefinition(RES_SDS_ATTR_ID, RES_SDS_ATTR_ID, "Maximum number of data blocks on the SDS (secondary data storage)", true, ""),
				new StringAttributeDefinition(SHELL_PATH_LIST_ATTR_ID, SHELL_PATH_LIST_ATTR_ID, "", true, ""),
				new StringAttributeDefinition(STAGEIN_ATTR_ID, STAGEIN_ATTR_ID, "", true, ""),
				new StringAttributeDefinition(STAGEOUT_ATTR_ID, STAGEOUT_ATTR_ID, "", true, ""),
				new StringAttributeDefinition(UMASK_ATTR_ID, UMASK_ATTR_ID, "", true, ""),
				new StringAttributeDefinition(USER_LIST_ATTR_ID, USER_LIST_ATTR_ID, "", true, ""),
				new StringAttributeDefinition(VARIABLE_LIST_ATTR_ID, VARIABLE_LIST_ATTR_ID, "", true, ""),
			};
	
	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return attrDefs;
	}
}

/*
 Resource_List.arch			string		System architecture
 Resource_List.cput			time		Maximum, aggregate CPU time required by all processes
 Resource_List.file			size		Maximum disk space requirements for any single file to be created
 Resource_List.host			string		Name of requested host/node
 Resource_List.mem			size		Maximum amount of physical memory (RAM)
 Resource_List.mpiprocs		int			Number of MPI processes for this chunk
 Resource_List.ncpus		int			Number of CPUs (processors)
 Resource_List.nice			int			Requested job priority
 Resource_List.nodes		string		Number and/or type of nodes
 Resource_List.nodect		int			Number of chunks in resource request from selection directive, or number of vnodes requested from node specification
 Resource_List.ompthreads	int			Number of OpenMP threads for this chunk.
 Resource_List.pcput		time		Per-process maximum CPU time
 Resource_List.pmem			size		Per-process maximum amount of physical memory
 Resource_List.pvmem		size		Per-process maximum amount of virtual memory
 Resource_List.resc			string		Single-node variable resource specification string
 Resource_List.vmem			size		Maximum, aggregate amount of virtual memory used by all concurrent processes
 Resource_List.walltime		time		Maximum amount of real time (wall-clock elapsed time)
 Resource_List.mppe			int			The number of processing elements used by a single process
 Resource_List.mppt			time		Maximum wallclock time used on the MPP.
 Resource_List.pf			size		Maximum number of file system blocks that can be used by all process
 Resource_List.pmppt		time		Maximum amount of wall clock time used on the MPP by a single process
 Resource_List.pncpus		int			Maximum number of processors used by any single process
 Resource_List.ppf			size		Maximum number of file system blocks that can be used by a single process
 Resource_List.procs		int			Maximum number of processes
 Resource_List.psds			size		Maximum number of data blocks on the SDS (secondary data storage) for any process
 Resource_List.sds			size		Maximum number of data blocks on the SDS (secondary data storage)
*/