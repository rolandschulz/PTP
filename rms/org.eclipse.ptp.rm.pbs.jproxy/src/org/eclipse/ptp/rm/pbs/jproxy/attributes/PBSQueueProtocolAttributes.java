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

public class PBSQueueProtocolAttributes {

	protected static final String RES_DEFAULT_ARCH_ATTR_ID = "Resource_List.default.arch"; //$NON-NLS-1$
	protected static final String RES_DEFAULT_CPUT_ATTR_ID = "Resource_List.default.cput"; //$NON-NLS-1$
	protected static final String RES_DEFAULT_FILE_ATTR_ID = "Resource_List.default.file"; //$NON-NLS-1$
	protected static final String RES_DEFAULT_HOST_ATTR_ID = "Resource_List.default.host"; //$NON-NLS-1$
	protected static final String RES_DEFAULT_MEM_ATTR_ID = "Resource_List.default.mem"; //$NON-NLS-1$
	protected static final String RES_DEFAULT_MPIPROCS_ATTR_ID = "Resource_List.default.mpiprocs"; //$NON-NLS-1$
	protected static final String RES_DEFAULT_NCPUS_ATTR_ID = "Resource_List.default.ncpus"; //$NON-NLS-1$
	protected static final String RES_DEFAULT_NICE_ATTR_ID = "Resource_List.default.nice"; //$NON-NLS-1$
	protected static final String RES_DEFAULT_NODES_ATTR_ID = "Resource_List.default.nodes"; //$NON-NLS-1$
	protected static final String RES_DEFAULT_NODECT_ATTR_ID = "Resource_List.default.nodect"; //$NON-NLS-1$
	protected static final String RES_DEFAULT_OMPTHREADS_ATTR_ID = "Resource_List.default.ompthreads"; //$NON-NLS-1$
	protected static final String RES_DEFAULT_PCPUT_ATTR_ID = "Resource_List.default.pcput"; //$NON-NLS-1$
	protected static final String RES_DEFAULT_PMEM_ATTR_ID = "Resource_List.default.pmem"; //$NON-NLS-1$
	protected static final String RES_DEFAULT_PVMEM_ATTR_ID = "Resource_List.default.pvmem"; //$NON-NLS-1$
	protected static final String RES_DEFAULT_RESC_ATTR_ID = "Resource_List.default.resc"; //$NON-NLS-1$
	protected static final String RES_DEFAULT_VMEM_ATTR_ID = "Resource_List.default.vmem"; //$NON-NLS-1$
	protected static final String RES_DEFAULT_WALLTIME_ATTR_ID = "Resource_List.default.walltime"; //$NON-NLS-1$
	protected static final String RES_DEFAULT_MPPE_ATTR_ID = "Resource_List.default.mppe"; //$NON-NLS-1$
	protected static final String RES_DEFAULT_MPPT_ATTR_ID = "Resource_List.default.mppt"; //$NON-NLS-1$
	protected static final String RES_DEFAULT_PF_ATTR_ID = "Resource_List.default.pf"; //$NON-NLS-1$
	protected static final String RES_DEFAULT_PMPPT_ATTR_ID = "Resource_List.default.pmppt"; //$NON-NLS-1$
	protected static final String RES_DEFAULT_PNCPUS_ATTR_ID = "Resource_List.default.pncpus"; //$NON-NLS-1$
	protected static final String RES_DEFAULT_PPF_ATTR_ID = "Resource_List.default.ppf"; //$NON-NLS-1$
	protected static final String RES_DEFAULT_PROCS_ATTR_ID = "Resource_List.default.procs"; //$NON-NLS-1$
	protected static final String RES_DEFAULT_PSDS_ATTR_ID = "Resource_List.default.psds"; //$NON-NLS-1$
	protected static final String RES_DEFAULT_SDS_ATTR_ID = "Resource_List.default.sds"; //$NON-NLS-1$
	protected static final String RES_MAX_ARCH_ATTR_ID = "Resource_List.max.arch"; //$NON-NLS-1$
	protected static final String RES_MAX_CPUT_ATTR_ID = "Resource_List.max.cput"; //$NON-NLS-1$
	protected static final String RES_MAX_FILE_ATTR_ID = "Resource_List.max.file"; //$NON-NLS-1$
	protected static final String RES_MAX_HOST_ATTR_ID = "Resource_List.max.host"; //$NON-NLS-1$
	protected static final String RES_MAX_MEM_ATTR_ID = "Resource_List.max.mem"; //$NON-NLS-1$
	protected static final String RES_MAX_MPIPROCS_ATTR_ID = "Resource_List.max.mpiprocs"; //$NON-NLS-1$
	protected static final String RES_MAX_NCPUS_ATTR_ID = "Resource_List.max.ncpus"; //$NON-NLS-1$
	protected static final String RES_MAX_NICE_ATTR_ID = "Resource_List.max.nice"; //$NON-NLS-1$
	protected static final String RES_MAX_NODES_ATTR_ID = "Resource_List.max.nodes"; //$NON-NLS-1$
	protected static final String RES_MAX_NODECT_ATTR_ID = "Resource_List.max.nodect"; //$NON-NLS-1$
	protected static final String RES_MAX_OMPTHREADS_ATTR_ID = "Resource_List.max.ompthreads"; //$NON-NLS-1$
	protected static final String RES_MAX_PCPUT_ATTR_ID = "Resource_List.max.pcput"; //$NON-NLS-1$
	protected static final String RES_MAX_PMEM_ATTR_ID = "Resource_List.max.pmem"; //$NON-NLS-1$
	protected static final String RES_MAX_PVMEM_ATTR_ID = "Resource_List.max.pvmem"; //$NON-NLS-1$
	protected static final String RES_MAX_RESC_ATTR_ID = "Resource_List.max.resc"; //$NON-NLS-1$
	protected static final String RES_MAX_VMEM_ATTR_ID = "Resource_List.max.vmem"; //$NON-NLS-1$
	protected static final String RES_MAX_WALLTIME_ATTR_ID = "Resource_List.max.walltime"; //$NON-NLS-1$
	protected static final String RES_MAX_MPPE_ATTR_ID = "Resource_List.max.mppe"; //$NON-NLS-1$
	protected static final String RES_MAX_MPPT_ATTR_ID = "Resource_List.max.mppt"; //$NON-NLS-1$
	protected static final String RES_MAX_PF_ATTR_ID = "Resource_List.max.pf"; //$NON-NLS-1$
	protected static final String RES_MAX_PMPPT_ATTR_ID = "Resource_List.max.pmppt"; //$NON-NLS-1$
	protected static final String RES_MAX_PNCPUS_ATTR_ID = "Resource_List.max.pncpus"; //$NON-NLS-1$
	protected static final String RES_MAX_PPF_ATTR_ID = "Resource_List.max.ppf"; //$NON-NLS-1$
	protected static final String RES_MAX_PROCS_ATTR_ID = "Resource_List.max.procs"; //$NON-NLS-1$
	protected static final String RES_MAX_PSDS_ATTR_ID = "Resource_List.max.psds"; //$NON-NLS-1$
	protected static final String RES_MAX_SDS_ATTR_ID = "Resource_List.max.sds"; //$NON-NLS-1$
	protected static final String RES_ASSIGNED_ARCH_ATTR_ID = "Resource_List.assigned.arch"; //$NON-NLS-1$
	protected static final String RES_ASSIGNED_CPUT_ATTR_ID = "Resource_List.assigned.cput"; //$NON-NLS-1$
	protected static final String RES_ASSIGNED_FILE_ATTR_ID = "Resource_List.assigned.file"; //$NON-NLS-1$
	protected static final String RES_ASSIGNED_HOST_ATTR_ID = "Resource_List.assigned.host"; //$NON-NLS-1$
	protected static final String RES_ASSIGNED_MEM_ATTR_ID = "Resource_List.assigned.mem"; //$NON-NLS-1$
	protected static final String RES_ASSIGNED_MPIPROCS_ATTR_ID = "Resource_List.assigned.mpiprocs"; //$NON-NLS-1$
	protected static final String RES_ASSIGNED_NCPUS_ATTR_ID = "Resource_List.assigned.ncpus"; //$NON-NLS-1$
	protected static final String RES_ASSIGNED_NICE_ATTR_ID = "Resource_List.assigned.nice"; //$NON-NLS-1$
	protected static final String RES_ASSIGNED_NODES_ATTR_ID = "Resource_List.assigned.nodes"; //$NON-NLS-1$
	protected static final String RES_ASSIGNED_NODECT_ATTR_ID = "Resource_List.assigned.nodect"; //$NON-NLS-1$
	protected static final String RES_ASSIGNED_OMPTHREADS_ATTR_ID = "Resource_List.assigned.ompthreads"; //$NON-NLS-1$
	protected static final String RES_ASSIGNED_PCPUT_ATTR_ID = "Resource_List.assigned.pcput"; //$NON-NLS-1$
	protected static final String RES_ASSIGNED_PMEM_ATTR_ID = "Resource_List.assigned.pmem"; //$NON-NLS-1$
	protected static final String RES_ASSIGNED_PVMEM_ATTR_ID = "Resource_List.assigned.pvmem"; //$NON-NLS-1$
	protected static final String RES_ASSIGNED_RESC_ATTR_ID = "Resource_List.assigned.resc"; //$NON-NLS-1$
	protected static final String RES_ASSIGNED_VMEM_ATTR_ID = "Resource_List.assigned.vmem"; //$NON-NLS-1$
	protected static final String RES_ASSIGNED_WALLTIME_ATTR_ID = "Resource_List.assigned.walltime"; //$NON-NLS-1$
	protected static final String RES_ASSIGNED_MPPE_ATTR_ID = "Resource_List.assigned.mppe"; //$NON-NLS-1$
	protected static final String RES_ASSIGNED_MPPT_ATTR_ID = "Resource_List.assigned.mppt"; //$NON-NLS-1$
	protected static final String RES_ASSIGNED_PF_ATTR_ID = "Resource_List.assigned.pf"; //$NON-NLS-1$
	protected static final String RES_ASSIGNED_PMPPT_ATTR_ID = "Resource_List.assigned.pmppt"; //$NON-NLS-1$
	protected static final String RES_ASSIGNED_PNCPUS_ATTR_ID = "Resource_List.assigned.pncpus"; //$NON-NLS-1$
	protected static final String RES_ASSIGNED_PPF_ATTR_ID = "Resource_List.assigned.ppf"; //$NON-NLS-1$
	protected static final String RES_ASSIGNED_PROCS_ATTR_ID = "Resource_List.assigned.procs"; //$NON-NLS-1$
	protected static final String RES_ASSIGNED_PSDS_ATTR_ID = "Resource_List.assigned.psds"; //$NON-NLS-1$
	protected static final String RES_ASSIGNED_SDS_ATTR_ID = "Resource_List.assigned.sds"; //$NON-NLS-1$
	protected static final String NAME_ATTR_ID = "name"; //$NON-NLS-1$
	protected static final String TYPE_ATTR_ID = "type"; //$NON-NLS-1$
	protected static final String TOTAL_JOBS_ATTR_ID = "total_jobs"; //$NON-NLS-1$
	protected static final String STATE_COUNT_ATTR_ID = "state_count"; //$NON-NLS-1$
	protected static final String MTIME_ATTR_ID = "mtime"; //$NON-NLS-1$
	protected static final String ENABLED_ATTR_ID = "enabled"; //$NON-NLS-1$
	protected static final String STARTED_ATTR_ID = "started"; //$NON-NLS-1$

}