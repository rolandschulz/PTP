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
import org.eclipse.ptp.rm.pbs.core.messages.Messages;
import org.eclipse.ptp.rm.pbs.jproxy.attributes.PBSJobProtocolAttributes;

/**
 * Job attributes
 */
public class PBSJobAttributes extends PBSJobProtocolAttributes {

	private static final IAttributeDefinition<?,?,?>[] attrDefs = new IAttributeDefinition[]{
				new StringAttributeDefinition(ACCOUNT_NAME_ATTR_ID, ACCOUNT_NAME_ATTR_ID, Messages.PBSJobAttributes_0, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(CHECKPOINT_ATTR_ID, CHECKPOINT_ATTR_ID, Messages.PBSJobAttributes_1, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(COMMENT_ATTR_ID, COMMENT_ATTR_ID, Messages.PBSJobAttributes_2, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(DEPEND_ATTR_ID, DEPEND_ATTR_ID, Messages.PBSJobAttributes_3, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(ERROR_PATH_ATTR_ID, ERROR_PATH_ATTR_ID, Messages.PBSJobAttributes_4, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(EXECUTION_TIME_ATTR_ID, EXECUTION_TIME_ATTR_ID, Messages.PBSJobAttributes_5, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(GROUP_LIST_ATTR_ID, GROUP_LIST_ATTR_ID, Messages.PBSJobAttributes_6, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(HOLD_TYPES_ATTR_ID, HOLD_TYPES_ATTR_ID, Messages.PBSJobAttributes_7, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(JOB_NAME_ATTR_ID, JOB_NAME_ATTR_ID, Messages.PBSJobAttributes_8, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(JOIN_PATH_ATTR_ID, JOIN_PATH_ATTR_ID, Messages.PBSJobAttributes_9, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(KEEP_FILES_ATTR_ID, KEEP_FILES_ATTR_ID, Messages.PBSJobAttributes_10, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(MAIL_POINTS_ATTR_ID, MAIL_POINTS_ATTR_ID, Messages.PBSJobAttributes_11, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(MAIL_USERS_ATTR_ID, MAIL_USERS_ATTR_ID, Messages.PBSJobAttributes_12, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(NO_STDIO_SOCKETS_ATTR_ID, NO_STDIO_SOCKETS_ATTR_ID, Messages.PBSJobAttributes_13, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(OUTPUT_PATH_ATTR_ID, OUTPUT_PATH_ATTR_ID, Messages.PBSJobAttributes_14, true, ""), //$NON-NLS-1$
				new IntegerAttributeDefinition(PRIORITY_ATTR_ID, PRIORITY_ATTR_ID, Messages.PBSJobAttributes_15, true, 0), 
				new StringAttributeDefinition(RERUNNABLE_ATTR_ID, RERUNNABLE_ATTR_ID, Messages.PBSJobAttributes_16, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(RES_ARCH_ATTR_ID, RES_ARCH_ATTR_ID, Messages.PBSJobAttributes_17, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(RES_CPUT_ATTR_ID, RES_CPUT_ATTR_ID, Messages.PBSJobAttributes_18, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(RES_FILE_ATTR_ID, RES_FILE_ATTR_ID, Messages.PBSJobAttributes_19, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(RES_HOST_ATTR_ID, RES_HOST_ATTR_ID, Messages.PBSJobAttributes_20, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(RES_MEM_ATTR_ID, RES_MEM_ATTR_ID, Messages.PBSJobAttributes_21, true, ""), //$NON-NLS-1$
				new IntegerAttributeDefinition(RES_MPIPROCS_ATTR_ID, RES_MPIPROCS_ATTR_ID, Messages.PBSJobAttributes_22, true, 0), 
				new IntegerAttributeDefinition(RES_NCPUS_ATTR_ID, RES_NCPUS_ATTR_ID, Messages.PBSJobAttributes_24, true, 0), 
				new IntegerAttributeDefinition(RES_NICE_ATTR_ID, RES_NICE_ATTR_ID, Messages.PBSJobAttributes_23, true, 0), 
				new StringAttributeDefinition(RES_NODES_ATTR_ID, RES_NODES_ATTR_ID, Messages.PBSJobAttributes_25, true, ""), //$NON-NLS-1$
				new IntegerAttributeDefinition(RES_NODECT_ATTR_ID, RES_NODECT_ATTR_ID, Messages.PBSJobAttributes_26, true, 0), 
				new IntegerAttributeDefinition(RES_OMPTHREADS_ATTR_ID, RES_OMPTHREADS_ATTR_ID, Messages.PBSJobAttributes_27, true, 0), 
				new StringAttributeDefinition(RES_PCPUT_ATTR_ID, RES_PCPUT_ATTR_ID, Messages.PBSJobAttributes_28, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(RES_PMEM_ATTR_ID, RES_PMEM_ATTR_ID, Messages.PBSJobAttributes_29, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(RES_PVMEM_ATTR_ID, RES_PVMEM_ATTR_ID, Messages.PBSJobAttributes_30, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(RES_RESC_ATTR_ID, RES_RESC_ATTR_ID, Messages.PBSJobAttributes_31, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(RES_VMEM_ATTR_ID, RES_VMEM_ATTR_ID, Messages.PBSJobAttributes_32, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(RES_WALLTIME_ATTR_ID, RES_WALLTIME_ATTR_ID, Messages.PBSJobAttributes_33, true, ""), //$NON-NLS-1$
				new IntegerAttributeDefinition(RES_MPPE_ATTR_ID, RES_MPPE_ATTR_ID, Messages.PBSJobAttributes_34, true, 0), 
				new StringAttributeDefinition(RES_MPPT_ATTR_ID, RES_MPPT_ATTR_ID, Messages.PBSJobAttributes_35, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(RES_PF_ATTR_ID, RES_PF_ATTR_ID, Messages.PBSJobAttributes_36, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(RES_PMPPT_ATTR_ID, RES_PMPPT_ATTR_ID, Messages.PBSJobAttributes_37, true, ""), //$NON-NLS-1$
				new IntegerAttributeDefinition(RES_PNCPUS_ATTR_ID, RES_PNCPUS_ATTR_ID, Messages.PBSJobAttributes_38, true, 0), 
				new StringAttributeDefinition(RES_PPF_ATTR_ID, RES_PPF_ATTR_ID, Messages.PBSJobAttributes_39, true, ""), //$NON-NLS-1$
				new IntegerAttributeDefinition(RES_PROCS_ATTR_ID, RES_PROCS_ATTR_ID, Messages.PBSJobAttributes_40, true, 0), 
				new StringAttributeDefinition(RES_PSDS_ATTR_ID, RES_PSDS_ATTR_ID, Messages.PBSJobAttributes_41, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(RES_SDS_ATTR_ID, RES_SDS_ATTR_ID, Messages.PBSJobAttributes_42, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(SHELL_PATH_LIST_ATTR_ID, SHELL_PATH_LIST_ATTR_ID, Messages.PBSJobAttributes_43, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(STAGEIN_ATTR_ID, STAGEIN_ATTR_ID, Messages.PBSJobAttributes_44, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(STAGEOUT_ATTR_ID, STAGEOUT_ATTR_ID, Messages.PBSJobAttributes_45, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(UMASK_ATTR_ID, UMASK_ATTR_ID, Messages.PBSJobAttributes_46, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(USER_LIST_ATTR_ID, USER_LIST_ATTR_ID, Messages.PBSJobAttributes_47, true, ""), //$NON-NLS-1$
				new StringAttributeDefinition(VARIABLE_LIST_ATTR_ID, VARIABLE_LIST_ATTR_ID, Messages.PBSJobAttributes_48, true, ""), //$NON-NLS-1$
			};
	
	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return attrDefs;
	}
}
