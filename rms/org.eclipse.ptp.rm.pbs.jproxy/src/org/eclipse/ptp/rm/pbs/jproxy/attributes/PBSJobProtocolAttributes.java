/*******************************************************************************
 * Copyright (c) 2010 The University of Tennessee,
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Roland Schulz - initial implementation

 *******************************************************************************/

package org.eclipse.ptp.rm.pbs.jproxy.attributes;

public class PBSJobProtocolAttributes {
	protected static final String ACCOUNT_NAME_ATTR_ID = "Account_Name"; //$NON-NLS-1$
	protected static final String CHECKPOINT_ATTR_ID = "Checkpoint"; //$NON-NLS-1$
	protected static final String COMMENT_ATTR_ID = "comment"; //$NON-NLS-1$
	protected static final String DEPEND_ATTR_ID = "depend"; //$NON-NLS-1$
	protected static final String ERROR_PATH_ATTR_ID = "Error_Path"; //$NON-NLS-1$
	protected static final String EXECUTION_TIME_ATTR_ID = "Execution_Time"; //$NON-NLS-1$
	protected static final String GROUP_LIST_ATTR_ID = "group_list"; //$NON-NLS-1$
	protected static final String HOLD_TYPES_ATTR_ID = "Hold_Types"; //$NON-NLS-1$
	protected static final String JOB_NAME_ATTR_ID = "Job_Name"; //$NON-NLS-1$
	protected static final String JOIN_PATH_ATTR_ID = "Join_Path"; //$NON-NLS-1$
	protected static final String KEEP_FILES_ATTR_ID = "Keep_Files"; //$NON-NLS-1$
	protected static final String MAIL_POINTS_ATTR_ID = "Mail_Points"; //$NON-NLS-1$
	protected static final String MAIL_USERS_ATTR_ID = "Mail_Users"; //$NON-NLS-1$
	protected static final String NO_STDIO_SOCKETS_ATTR_ID = "no_stdio_sockets"; //$NON-NLS-1$
	protected static final String OUTPUT_PATH_ATTR_ID = "Output_Path"; //$NON-NLS-1$
	protected static final String PRIORITY_ATTR_ID = "Priority"; //$NON-NLS-1$
	protected static final String RERUNNABLE_ATTR_ID = "Rerunnable"; //$NON-NLS-1$
	protected static final String RES_ARCH_ATTR_ID = "Resource_List.arch"; //$NON-NLS-1$
	protected static final String RES_CPUT_ATTR_ID = "Resource_List.cput"; //$NON-NLS-1$
	protected static final String RES_FILE_ATTR_ID = "Resource_List.file"; //$NON-NLS-1$
	protected static final String RES_HOST_ATTR_ID = "Resource_List.host"; //$NON-NLS-1$
	protected static final String RES_MEM_ATTR_ID = "Resource_List.mem"; //$NON-NLS-1$
	protected static final String RES_MPIPROCS_ATTR_ID = "Resource_List.mpiprocs"; //$NON-NLS-1$
	protected static final String RES_NCPUS_ATTR_ID = "Resource_List.ncpus"; //$NON-NLS-1$
	protected static final String RES_NICE_ATTR_ID = "Resource_List.nice"; //$NON-NLS-1$
	protected static final String RES_NODES_ATTR_ID = "Resource_List.nodes"; //$NON-NLS-1$
	protected static final String RES_NODECT_ATTR_ID = "Resource_List.nodect"; //$NON-NLS-1$
	protected static final String RES_OMPTHREADS_ATTR_ID = "Resource_List.ompthreads"; //$NON-NLS-1$
	protected static final String RES_PCPUT_ATTR_ID = "Resource_List.pcput"; //$NON-NLS-1$
	protected static final String RES_PMEM_ATTR_ID = "Resource_List.pmem"; //$NON-NLS-1$
	protected static final String RES_PVMEM_ATTR_ID = "Resource_List.pvmem"; //$NON-NLS-1$
	protected static final String RES_RESC_ATTR_ID = "Resource_List.resc"; //$NON-NLS-1$
	protected static final String RES_VMEM_ATTR_ID = "Resource_List.vmem"; //$NON-NLS-1$
	protected static final String RES_WALLTIME_ATTR_ID = "Resource_List.walltime"; //$NON-NLS-1$
	protected static final String RES_MPPE_ATTR_ID = "Resource_List.mppe"; //$NON-NLS-1$
	protected static final String RES_MPPT_ATTR_ID = "Resource_List.mppt"; //$NON-NLS-1$
	protected static final String RES_PF_ATTR_ID = "Resource_List.pf"; //$NON-NLS-1$
	protected static final String RES_PMPPT_ATTR_ID = "Resource_List.pmppt"; //$NON-NLS-1$
	protected static final String RES_PNCPUS_ATTR_ID = "Resource_List.pncpus"; //$NON-NLS-1$
	protected static final String RES_PPF_ATTR_ID = "Resource_List.ppf"; //$NON-NLS-1$
	protected static final String RES_PROCS_ATTR_ID = "Resource_List.procs"; //$NON-NLS-1$
	protected static final String RES_PSDS_ATTR_ID = "Resource_List.psds"; //$NON-NLS-1$
	protected static final String RES_SDS_ATTR_ID = "Resource_List.sds"; //$NON-NLS-1$
	protected static final String SHELL_PATH_LIST_ATTR_ID = "Shell_Path_List"; //$NON-NLS-1$
	protected static final String STAGEIN_ATTR_ID = "stagein"; //$NON-NLS-1$
	protected static final String STAGEOUT_ATTR_ID = "stageout"; //$NON-NLS-1$
	protected static final String UMASK_ATTR_ID = "umask"; //$NON-NLS-1$
	protected static final String USER_LIST_ATTR_ID = "User_List"; //$NON-NLS-1$
	protected static final String VARIABLE_LIST_ATTR_ID = "Variable_List"; //$NON-NLS-1$

}
