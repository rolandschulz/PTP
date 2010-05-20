/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *     Dieter Krachtus, University of Heidelberg
 *     Roland Schulz, University of Tennessee
 *******************************************************************************/
package org.eclipse.ptp.rm.pbs.core;

import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;
import org.eclipse.ptp.rm.pbs.core.messages.Messages;
import org.eclipse.ptp.rm.pbs.jproxy.attributes.PBSQueueProtocolAttributes;

/**
 * Job attributes
 * 
 * @since 4.0
 */
public class PBSQueueAttributes extends PBSQueueProtocolAttributes {

	private static final IAttributeDefinition<?, ?, ?>[] attrDefs = new IAttributeDefinition[] {
			new StringAttributeDefinition(NAME_ATTR_ID, NAME_ATTR_ID, Messages.PBSQueueAttributes_0, true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(TYPE_ATTR_ID, TYPE_ATTR_ID, Messages.PBSQueueAttributes_1, true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(TOTAL_JOBS_ATTR_ID, TOTAL_JOBS_ATTR_ID, Messages.PBSQueueAttributes_2, true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(STATE_COUNT_ATTR_ID, STATE_COUNT_ATTR_ID, Messages.PBSQueueAttributes_3, true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(MTIME_ATTR_ID, MTIME_ATTR_ID, Messages.PBSQueueAttributes_4, true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(ENABLED_ATTR_ID, ENABLED_ATTR_ID, Messages.PBSQueueAttributes_5, true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(STARTED_ATTR_ID, STARTED_ATTR_ID, Messages.PBSQueueAttributes_6, true, ""), //$NON-NLS-1$

			new StringAttributeDefinition(RES_DEFAULT_ARCH_ATTR_ID, RES_DEFAULT_ARCH_ATTR_ID, Messages.PBSQueueAttributes_7, true,
					""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_DEFAULT_CPUT_ATTR_ID, RES_DEFAULT_CPUT_ATTR_ID, Messages.PBSQueueAttributes_8, true,
					""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_DEFAULT_FILE_ATTR_ID, RES_DEFAULT_FILE_ATTR_ID, Messages.PBSQueueAttributes_9, true,
					""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_DEFAULT_HOST_ATTR_ID, RES_DEFAULT_HOST_ATTR_ID, Messages.PBSQueueAttributes_10, true,
					""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_DEFAULT_MEM_ATTR_ID, RES_DEFAULT_MEM_ATTR_ID, Messages.PBSQueueAttributes_11, true,
					""), //$NON-NLS-1$
			new IntegerAttributeDefinition(RES_DEFAULT_MPIPROCS_ATTR_ID, RES_DEFAULT_MPIPROCS_ATTR_ID,
					Messages.PBSQueueAttributes_12, true, 0),
			new IntegerAttributeDefinition(RES_DEFAULT_NCPUS_ATTR_ID, RES_DEFAULT_NCPUS_ATTR_ID, Messages.PBSQueueAttributes_13,
					true, 0),
			new IntegerAttributeDefinition(RES_DEFAULT_NICE_ATTR_ID, RES_DEFAULT_NICE_ATTR_ID, Messages.PBSQueueAttributes_14,
					true, 0),
			new StringAttributeDefinition(RES_DEFAULT_NODES_ATTR_ID, RES_DEFAULT_NODES_ATTR_ID, Messages.PBSQueueAttributes_15,
					true, ""), //$NON-NLS-1$
			new IntegerAttributeDefinition(RES_DEFAULT_NODECT_ATTR_ID, RES_DEFAULT_NODECT_ATTR_ID, Messages.PBSQueueAttributes_16,
					true, 0),
			new IntegerAttributeDefinition(RES_DEFAULT_OMPTHREADS_ATTR_ID, RES_DEFAULT_OMPTHREADS_ATTR_ID,
					Messages.PBSQueueAttributes_17, true, 0),
			new StringAttributeDefinition(RES_DEFAULT_PCPUT_ATTR_ID, RES_DEFAULT_PCPUT_ATTR_ID, Messages.PBSQueueAttributes_18,
					true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_DEFAULT_PMEM_ATTR_ID, RES_DEFAULT_PMEM_ATTR_ID, Messages.PBSQueueAttributes_19, true,
					""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_DEFAULT_PVMEM_ATTR_ID, RES_DEFAULT_PVMEM_ATTR_ID, Messages.PBSQueueAttributes_20,
					true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_DEFAULT_RESC_ATTR_ID, RES_DEFAULT_RESC_ATTR_ID, Messages.PBSQueueAttributes_21, true,
					""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_DEFAULT_VMEM_ATTR_ID, RES_DEFAULT_VMEM_ATTR_ID, Messages.PBSQueueAttributes_22, true,
					""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_DEFAULT_WALLTIME_ATTR_ID, RES_DEFAULT_WALLTIME_ATTR_ID,
					Messages.PBSQueueAttributes_23, true, ""), //$NON-NLS-1$
			new IntegerAttributeDefinition(RES_DEFAULT_MPPE_ATTR_ID, RES_DEFAULT_MPPE_ATTR_ID, Messages.PBSQueueAttributes_24,
					true, 0),
			new StringAttributeDefinition(RES_DEFAULT_MPPT_ATTR_ID, RES_DEFAULT_MPPT_ATTR_ID, Messages.PBSQueueAttributes_25, true,
					""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_DEFAULT_PF_ATTR_ID, RES_DEFAULT_PF_ATTR_ID, Messages.PBSQueueAttributes_26, true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_DEFAULT_PMPPT_ATTR_ID, RES_DEFAULT_PMPPT_ATTR_ID, Messages.PBSQueueAttributes_27,
					true, ""), //$NON-NLS-1$
			new IntegerAttributeDefinition(RES_DEFAULT_PNCPUS_ATTR_ID, RES_DEFAULT_PNCPUS_ATTR_ID, Messages.PBSQueueAttributes_28,
					true, 0),
			new StringAttributeDefinition(RES_DEFAULT_PPF_ATTR_ID, RES_DEFAULT_PPF_ATTR_ID, Messages.PBSQueueAttributes_29, true,
					""), //$NON-NLS-1$
			new IntegerAttributeDefinition(RES_DEFAULT_PROCS_ATTR_ID, RES_DEFAULT_PROCS_ATTR_ID, Messages.PBSQueueAttributes_30,
					true, 0),
			new StringAttributeDefinition(RES_DEFAULT_PSDS_ATTR_ID, RES_DEFAULT_PSDS_ATTR_ID, Messages.PBSQueueAttributes_31, true,
					""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_DEFAULT_SDS_ATTR_ID, RES_DEFAULT_SDS_ATTR_ID, Messages.PBSQueueAttributes_32, true,
					""), //$NON-NLS-1$

			new StringAttributeDefinition(RES_MAX_ARCH_ATTR_ID, RES_MAX_ARCH_ATTR_ID, Messages.PBSQueueAttributes_33, true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_MAX_CPUT_ATTR_ID, RES_MAX_CPUT_ATTR_ID, Messages.PBSQueueAttributes_34, true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_MAX_FILE_ATTR_ID, RES_MAX_FILE_ATTR_ID, Messages.PBSQueueAttributes_35, true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_MAX_HOST_ATTR_ID, RES_MAX_HOST_ATTR_ID, Messages.PBSQueueAttributes_36, true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_MAX_MEM_ATTR_ID, RES_MAX_MEM_ATTR_ID, Messages.PBSQueueAttributes_37, true, ""), //$NON-NLS-1$
			new IntegerAttributeDefinition(RES_MAX_MPIPROCS_ATTR_ID, RES_MAX_MPIPROCS_ATTR_ID, Messages.PBSQueueAttributes_38,
					true, 0),
			new IntegerAttributeDefinition(RES_MAX_NCPUS_ATTR_ID, RES_MAX_NCPUS_ATTR_ID, Messages.PBSQueueAttributes_39, true, 0),
			new IntegerAttributeDefinition(RES_MAX_NICE_ATTR_ID, RES_MAX_NICE_ATTR_ID, Messages.PBSQueueAttributes_40, true, 0),
			new StringAttributeDefinition(RES_MAX_NODES_ATTR_ID, RES_MAX_NODES_ATTR_ID, Messages.PBSQueueAttributes_41, true, ""), //$NON-NLS-1$
			new IntegerAttributeDefinition(RES_MAX_NODECT_ATTR_ID, RES_MAX_NODECT_ATTR_ID, Messages.PBSQueueAttributes_42, true, 0),
			new IntegerAttributeDefinition(RES_MAX_OMPTHREADS_ATTR_ID, RES_MAX_OMPTHREADS_ATTR_ID, Messages.PBSQueueAttributes_43,
					true, 0),
			new StringAttributeDefinition(RES_MAX_PCPUT_ATTR_ID, RES_MAX_PCPUT_ATTR_ID, Messages.PBSQueueAttributes_44, true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_MAX_PMEM_ATTR_ID, RES_MAX_PMEM_ATTR_ID, Messages.PBSQueueAttributes_45, true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_MAX_PVMEM_ATTR_ID, RES_MAX_PVMEM_ATTR_ID, Messages.PBSQueueAttributes_46, true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_MAX_RESC_ATTR_ID, RES_MAX_RESC_ATTR_ID, Messages.PBSQueueAttributes_47, true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_MAX_VMEM_ATTR_ID, RES_MAX_VMEM_ATTR_ID, Messages.PBSQueueAttributes_48, true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_MAX_WALLTIME_ATTR_ID, RES_MAX_WALLTIME_ATTR_ID, Messages.PBSQueueAttributes_49, true,
					""), //$NON-NLS-1$
			new IntegerAttributeDefinition(RES_MAX_MPPE_ATTR_ID, RES_MAX_MPPE_ATTR_ID, Messages.PBSQueueAttributes_50, true, 0),
			new StringAttributeDefinition(RES_MAX_MPPT_ATTR_ID, RES_MAX_MPPT_ATTR_ID, Messages.PBSQueueAttributes_51, true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_MAX_PF_ATTR_ID, RES_MAX_PF_ATTR_ID, Messages.PBSQueueAttributes_52, true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_MAX_PMPPT_ATTR_ID, RES_MAX_PMPPT_ATTR_ID, Messages.PBSQueueAttributes_53, true, ""), //$NON-NLS-1$
			new IntegerAttributeDefinition(RES_MAX_PNCPUS_ATTR_ID, RES_MAX_PNCPUS_ATTR_ID, Messages.PBSQueueAttributes_54, true, 0),
			new StringAttributeDefinition(RES_MAX_PPF_ATTR_ID, RES_MAX_PPF_ATTR_ID, Messages.PBSQueueAttributes_55, true, ""), //$NON-NLS-1$
			new IntegerAttributeDefinition(RES_MAX_PROCS_ATTR_ID, RES_MAX_PROCS_ATTR_ID, Messages.PBSQueueAttributes_56, true, 0),
			new StringAttributeDefinition(RES_MAX_PSDS_ATTR_ID, RES_MAX_PSDS_ATTR_ID, Messages.PBSQueueAttributes_57, true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_MAX_SDS_ATTR_ID, RES_MAX_SDS_ATTR_ID, Messages.PBSQueueAttributes_58, true, ""), //$NON-NLS-1$

			new StringAttributeDefinition(RES_ASSIGNED_ARCH_ATTR_ID, RES_ASSIGNED_ARCH_ATTR_ID, Messages.PBSQueueAttributes_59,
					true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_ASSIGNED_CPUT_ATTR_ID, RES_ASSIGNED_CPUT_ATTR_ID, Messages.PBSQueueAttributes_60,
					true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_ASSIGNED_FILE_ATTR_ID, RES_ASSIGNED_FILE_ATTR_ID, Messages.PBSQueueAttributes_61,
					true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_ASSIGNED_HOST_ATTR_ID, RES_ASSIGNED_HOST_ATTR_ID, Messages.PBSQueueAttributes_62,
					true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_ASSIGNED_MEM_ATTR_ID, RES_ASSIGNED_MEM_ATTR_ID, Messages.PBSQueueAttributes_63, true,
					""), //$NON-NLS-1$
			new IntegerAttributeDefinition(RES_ASSIGNED_MPIPROCS_ATTR_ID, RES_ASSIGNED_MPIPROCS_ATTR_ID,
					Messages.PBSQueueAttributes_64, true, 0),
			new IntegerAttributeDefinition(RES_ASSIGNED_NCPUS_ATTR_ID, RES_ASSIGNED_NCPUS_ATTR_ID, Messages.PBSQueueAttributes_65,
					true, 0),
			new IntegerAttributeDefinition(RES_ASSIGNED_NICE_ATTR_ID, RES_ASSIGNED_NICE_ATTR_ID, Messages.PBSQueueAttributes_66,
					true, 0),
			new StringAttributeDefinition(RES_ASSIGNED_NODES_ATTR_ID, RES_ASSIGNED_NODES_ATTR_ID, Messages.PBSQueueAttributes_67,
					true, ""), //$NON-NLS-1$
			new IntegerAttributeDefinition(RES_ASSIGNED_NODECT_ATTR_ID, RES_ASSIGNED_NODECT_ATTR_ID,
					Messages.PBSQueueAttributes_68, true, 0),
			new IntegerAttributeDefinition(RES_ASSIGNED_OMPTHREADS_ATTR_ID, RES_ASSIGNED_OMPTHREADS_ATTR_ID,
					Messages.PBSQueueAttributes_69, true, 0),
			new StringAttributeDefinition(RES_ASSIGNED_PCPUT_ATTR_ID, RES_ASSIGNED_PCPUT_ATTR_ID, Messages.PBSQueueAttributes_70,
					true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_ASSIGNED_PMEM_ATTR_ID, RES_ASSIGNED_PMEM_ATTR_ID, Messages.PBSQueueAttributes_71,
					true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_ASSIGNED_PVMEM_ATTR_ID, RES_ASSIGNED_PVMEM_ATTR_ID, Messages.PBSQueueAttributes_72,
					true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_ASSIGNED_RESC_ATTR_ID, RES_ASSIGNED_RESC_ATTR_ID, Messages.PBSQueueAttributes_73,
					true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_ASSIGNED_VMEM_ATTR_ID, RES_ASSIGNED_VMEM_ATTR_ID, Messages.PBSQueueAttributes_74,
					true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_ASSIGNED_WALLTIME_ATTR_ID, RES_ASSIGNED_WALLTIME_ATTR_ID,
					Messages.PBSQueueAttributes_75, true, ""), //$NON-NLS-1$
			new IntegerAttributeDefinition(RES_ASSIGNED_MPPE_ATTR_ID, RES_ASSIGNED_MPPE_ATTR_ID, Messages.PBSQueueAttributes_76,
					true, 0),
			new StringAttributeDefinition(RES_ASSIGNED_MPPT_ATTR_ID, RES_ASSIGNED_MPPT_ATTR_ID, Messages.PBSQueueAttributes_77,
					true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_ASSIGNED_PF_ATTR_ID, RES_ASSIGNED_PF_ATTR_ID, Messages.PBSQueueAttributes_78, true,
					""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_ASSIGNED_PMPPT_ATTR_ID, RES_ASSIGNED_PMPPT_ATTR_ID, Messages.PBSQueueAttributes_79,
					true, ""), //$NON-NLS-1$
			new IntegerAttributeDefinition(RES_ASSIGNED_PNCPUS_ATTR_ID, RES_ASSIGNED_PNCPUS_ATTR_ID,
					Messages.PBSQueueAttributes_80, true, 0),
			new StringAttributeDefinition(RES_ASSIGNED_PPF_ATTR_ID, RES_ASSIGNED_PPF_ATTR_ID, Messages.PBSQueueAttributes_81, true,
					""), //$NON-NLS-1$
			new IntegerAttributeDefinition(RES_ASSIGNED_PROCS_ATTR_ID, RES_ASSIGNED_PROCS_ATTR_ID, Messages.PBSQueueAttributes_82,
					true, 0),
			new StringAttributeDefinition(RES_ASSIGNED_PSDS_ATTR_ID, RES_ASSIGNED_PSDS_ATTR_ID, Messages.PBSQueueAttributes_83,
					true, ""), //$NON-NLS-1$
			new StringAttributeDefinition(RES_ASSIGNED_SDS_ATTR_ID, RES_ASSIGNED_SDS_ATTR_ID, Messages.PBSQueueAttributes_84, true,
					""), //$NON-NLS-1$
	};

	public static IAttributeDefinition<?, ?, ?>[] getDefaultAttributeDefinitions() {
		return attrDefs;
	}
}
