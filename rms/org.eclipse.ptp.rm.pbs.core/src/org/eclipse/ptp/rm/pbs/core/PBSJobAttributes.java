/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * 	IBM Corporation - Initial API and implementatio
 * 	Albert L. Rossi (NCSA) - Updated attributes (bug 310189)
 *******************************************************************************/
package org.eclipse.ptp.rm.pbs.core;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;
import org.eclipse.ptp.rm.pbs.core.messages.Messages;

/**
 * Hard-coded mapping of known (static) PBS Job Attributes.
 * 
 * @author arossi
 * 
 */
public class PBSJobAttributes {

	private static final Map<String, IAttributeDefinition<?, ?, ?>> attrMap;

	static {
		attrMap = new TreeMap<String, IAttributeDefinition<?, ?, ?>>();
		attrMap.put(Messages.PBSJobAttributeName_0, // ACCOUNT_NAME
				new StringAttributeDefinition(Messages.PBSJobAttributeName_0, Messages.PBSJobAttributeName_0,
						Messages.PBSJobAttributeDescription_0, true, Messages.PBSJobAttributeDefaultValue_0));
		attrMap.put(Messages.PBSJobAttributeName_1, // CHECKPOINT_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_1, Messages.PBSJobAttributeName_1,
						Messages.PBSJobAttributeDescription_1, true, Messages.PBSJobAttributeDefaultValue_1));
		attrMap.put(Messages.PBSJobAttributeName_2, // COMMENT_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_2, Messages.PBSJobAttributeName_2,
						Messages.PBSJobAttributeDescription_2, true, Messages.PBSJobAttributeDefaultValue_2));
		attrMap.put(Messages.PBSJobAttributeName_3, // DEPEND_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_3, Messages.PBSJobAttributeName_3,
						Messages.PBSJobAttributeDescription_3, true, Messages.PBSJobAttributeDefaultValue_3));
		attrMap.put(Messages.PBSJobAttributeName_4, // ERROR_PATH_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_4, Messages.PBSJobAttributeName_4,
						Messages.PBSJobAttributeDescription_4, true, Messages.PBSJobAttributeDefaultValue_4));
		attrMap.put(Messages.PBSJobAttributeName_5, // EXECUTION_TIME_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_5, Messages.PBSJobAttributeName_5,
						Messages.PBSJobAttributeDescription_5, true, Messages.PBSJobAttributeDefaultValue_5));
		attrMap.put(Messages.PBSJobAttributeName_6, // GROUP_LIST_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_6, Messages.PBSJobAttributeName_6,
						Messages.PBSJobAttributeDescription_6, true, Messages.PBSJobAttributeDefaultValue_6));
		attrMap.put(Messages.PBSJobAttributeName_7, // HOLD_TYPES_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_7, Messages.PBSJobAttributeName_7,
						Messages.PBSJobAttributeDescription_7, true, Messages.PBSJobAttributeDefaultValue_7));
		attrMap.put(Messages.PBSJobAttributeName_8, // JOB_NAME_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_8, Messages.PBSJobAttributeName_8,
						Messages.PBSJobAttributeDescription_8, true, Messages.PBSJobAttributeDefaultValue_8));
		attrMap.put(Messages.PBSJobAttributeName_9, // JOIN_PATH_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_9, Messages.PBSJobAttributeName_9,
						Messages.PBSJobAttributeDescription_9, true, Messages.PBSJobAttributeDefaultValue_9));
		attrMap.put(Messages.PBSJobAttributeName_10, // KEEP_FILES_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_10, Messages.PBSJobAttributeName_10,
						Messages.PBSJobAttributeDescription_10, true, Messages.PBSJobAttributeDefaultValue_10));
		attrMap.put(Messages.PBSJobAttributeName_11, // MAIL_POINTS_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_11, Messages.PBSJobAttributeName_11,
						Messages.PBSJobAttributeDescription_11, true, Messages.PBSJobAttributeDefaultValue_11));
		attrMap.put(Messages.PBSJobAttributeName_12, // MAIL_USERS_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_12, Messages.PBSJobAttributeName_12,
						Messages.PBSJobAttributeDescription_12, true, Messages.PBSJobAttributeDefaultValue_12));
		attrMap.put(Messages.PBSJobAttributeName_13, // NO_STDIO_SOCKETS_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_13, Messages.PBSJobAttributeName_13,
						Messages.PBSJobAttributeDescription_13, true, Messages.PBSJobAttributeDefaultValue_13));
		attrMap.put(Messages.PBSJobAttributeName_14, // OUTPUT_PATH_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_14, Messages.PBSJobAttributeName_14,
						Messages.PBSJobAttributeDescription_14, true, Messages.PBSJobAttributeDefaultValue_14));
		attrMap.put(Messages.PBSJobAttributeName_15, // PRIORITY_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_15, Messages.PBSJobAttributeName_15,
						Messages.PBSJobAttributeDescription_15, true, Messages.PBSJobAttributeDefaultValue_15));
		attrMap.put(Messages.PBSJobAttributeName_16, // RERUNNABLE_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_16, Messages.PBSJobAttributeName_16,
						Messages.PBSJobAttributeDescription_16, true, Messages.PBSJobAttributeDefaultValue_16));
		attrMap.put(Messages.PBSJobAttributeName_17, // RES_ARCH_ATTR_ID
				new StringAttributeDefinition(Messages.PBSJobAttributeName_17, Messages.PBSJobAttributeName_17,
						Messages.PBSJobAttributeDescription_17, true, Messages.PBSJobAttributeDefaultValue_17));
		attrMap.put(Messages.PBSJobAttributeName_18, // RES_CPUT_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_18, Messages.PBSJobAttributeName_18,
						Messages.PBSJobAttributeDescription_18, true, Messages.PBSJobAttributeDefaultValue_18));
		attrMap.put(Messages.PBSJobAttributeName_19, // RES_FILE_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_19, Messages.PBSJobAttributeName_19,
						Messages.PBSJobAttributeDescription_19, true, Messages.PBSJobAttributeDefaultValue_19));
		attrMap.put(Messages.PBSJobAttributeName_20, // RES_HOST_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_20, Messages.PBSJobAttributeName_20,
						Messages.PBSJobAttributeDescription_20, true, Messages.PBSJobAttributeDefaultValue_20));
		attrMap.put(Messages.PBSJobAttributeName_21, // RES_MEM_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_21, Messages.PBSJobAttributeName_21,
						Messages.PBSJobAttributeDescription_21, true, Messages.PBSJobAttributeDefaultValue_21));
		attrMap.put(Messages.PBSJobAttributeName_22, // RES_MPIPROCS_ATTR
				new IntegerAttributeDefinition(Messages.PBSJobAttributeName_22, Messages.PBSJobAttributeName_22,
						Messages.PBSJobAttributeDescription_22, true, new Integer(
								Messages.PBSJobAttributeDefaultValue_22)));
		attrMap.put(Messages.PBSJobAttributeName_23, // RES_NCPUS_ATTR_ID
				new IntegerAttributeDefinition(Messages.PBSJobAttributeName_23, Messages.PBSJobAttributeName_23,
						Messages.PBSJobAttributeDescription_23, true, new Integer(
								Messages.PBSJobAttributeDefaultValue_23)));
		attrMap.put(Messages.PBSJobAttributeName_24, // RES_NICE_ATTR_ID
				new IntegerAttributeDefinition(Messages.PBSJobAttributeName_24, Messages.PBSJobAttributeName_24,
						Messages.PBSJobAttributeDescription_24, true, new Integer(
								Messages.PBSJobAttributeDefaultValue_24)));
		attrMap.put(Messages.PBSJobAttributeName_25, // RES_NODES_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_25, Messages.PBSJobAttributeName_25,
						Messages.PBSJobAttributeDescription_25, true, Messages.PBSJobAttributeDefaultValue_25));
		attrMap.put(Messages.PBSJobAttributeName_26, // RES_NODECT_ATTR
				new IntegerAttributeDefinition(Messages.PBSJobAttributeName_26, Messages.PBSJobAttributeName_26,
						Messages.PBSJobAttributeDescription_26, true, new Integer(
								Messages.PBSJobAttributeDefaultValue_26)));
		attrMap.put(Messages.PBSJobAttributeName_27, // RES_OMPTHREADS_ATTR
				new IntegerAttributeDefinition(Messages.PBSJobAttributeName_27, Messages.PBSJobAttributeName_27,
						Messages.PBSJobAttributeDescription_27, true, new Integer(
								Messages.PBSJobAttributeDefaultValue_27)));
		attrMap.put(Messages.PBSJobAttributeName_28, // RES_PCPUT_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_28, Messages.PBSJobAttributeName_28,
						Messages.PBSJobAttributeDescription_28, true, Messages.PBSJobAttributeDefaultValue_28));
		attrMap.put(Messages.PBSJobAttributeName_29, // RES_PMEM_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_29, Messages.PBSJobAttributeName_29,
						Messages.PBSJobAttributeDescription_29, true, Messages.PBSJobAttributeDefaultValue_29));
		attrMap.put(Messages.PBSJobAttributeName_30, // RES_PVMEM_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_30, Messages.PBSJobAttributeName_30,
						Messages.PBSJobAttributeDescription_30, true, Messages.PBSJobAttributeDefaultValue_30));
		attrMap.put(Messages.PBSJobAttributeName_31, // RES_RESC_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_31, Messages.PBSJobAttributeName_31,
						Messages.PBSJobAttributeDescription_31, true, Messages.PBSJobAttributeDefaultValue_31));
		attrMap.put(Messages.PBSJobAttributeName_32, // RES_VMEM_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_32, Messages.PBSJobAttributeName_32,
						Messages.PBSJobAttributeDescription_32, true, Messages.PBSJobAttributeDefaultValue_32));
		attrMap.put(Messages.PBSJobAttributeName_33, // RES_WALLTIME_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_33, Messages.PBSJobAttributeName_33,
						Messages.PBSJobAttributeDescription_33, true, Messages.PBSJobAttributeDefaultValue_33));
		attrMap.put(Messages.PBSJobAttributeName_34, // RES_MPPE_ATTR
				new IntegerAttributeDefinition(Messages.PBSJobAttributeName_34, Messages.PBSJobAttributeName_34,
						Messages.PBSJobAttributeDescription_34, true, new Integer(
								Messages.PBSJobAttributeDefaultValue_34)));
		attrMap.put(Messages.PBSJobAttributeName_35, // RES_MPPT_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_35, Messages.PBSJobAttributeName_35,
						Messages.PBSJobAttributeDescription_35, true, Messages.PBSJobAttributeDefaultValue_35));
		attrMap.put(Messages.PBSJobAttributeName_36, // RES_PF_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_36, Messages.PBSJobAttributeName_36,
						Messages.PBSJobAttributeDescription_36, true, Messages.PBSJobAttributeDefaultValue_36));
		attrMap.put(Messages.PBSJobAttributeName_37, // RES_PMPPT_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_37, Messages.PBSJobAttributeName_37,
						Messages.PBSJobAttributeDescription_37, true, Messages.PBSJobAttributeDefaultValue_37));
		attrMap.put(Messages.PBSJobAttributeName_38, // RES_PNCPUS_ATTR_ID
				new IntegerAttributeDefinition(Messages.PBSJobAttributeName_38, Messages.PBSJobAttributeName_38,
						Messages.PBSJobAttributeDescription_38, true, new Integer(
								Messages.PBSJobAttributeDefaultValue_38)));
		attrMap.put(Messages.PBSJobAttributeName_39, // RES_PPF_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_39, Messages.PBSJobAttributeName_39,
						Messages.PBSJobAttributeDescription_39, true, Messages.PBSJobAttributeDefaultValue_39));
		attrMap.put(Messages.PBSJobAttributeName_40, // RES_PROCS_ATTR
				new IntegerAttributeDefinition(Messages.PBSJobAttributeName_40, Messages.PBSJobAttributeName_40,
						Messages.PBSJobAttributeDescription_40, true, new Integer(
								Messages.PBSJobAttributeDefaultValue_40)));
		attrMap.put(Messages.PBSJobAttributeName_41, // RES_PSDS_ATTR
				new IntegerAttributeDefinition(Messages.PBSJobAttributeName_41, Messages.PBSJobAttributeName_41,
						Messages.PBSJobAttributeDescription_41, true, new Integer(
								Messages.PBSJobAttributeDefaultValue_41)));
		attrMap.put(Messages.PBSJobAttributeName_42, // RES_SDS_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_42, Messages.PBSJobAttributeName_42,
						Messages.PBSJobAttributeDescription_42, true, Messages.PBSJobAttributeDefaultValue_42));
		attrMap.put(Messages.PBSJobAttributeName_43, // SHELL_PATH_LIST_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_43, Messages.PBSJobAttributeName_43,
						Messages.PBSJobAttributeDescription_43, true, Messages.PBSJobAttributeDefaultValue_43));
		attrMap.put(Messages.PBSJobAttributeName_44, // STAGEIN_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_44, Messages.PBSJobAttributeName_44,
						Messages.PBSJobAttributeDescription_44, true, Messages.PBSJobAttributeDefaultValue_44));
		attrMap.put(Messages.PBSJobAttributeName_45, // STAGEOUT_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_45, Messages.PBSJobAttributeName_45,
						Messages.PBSJobAttributeDescription_45, true, Messages.PBSJobAttributeDefaultValue_45));
		attrMap.put(Messages.PBSJobAttributeName_46, // UMASK_ATTR_ID
				new StringAttributeDefinition(Messages.PBSJobAttributeName_46, Messages.PBSJobAttributeName_46,
						Messages.PBSJobAttributeDescription_46, true, Messages.PBSJobAttributeDefaultValue_46));
		attrMap.put(Messages.PBSJobAttributeName_47, // USER_LIST_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_47, Messages.PBSJobAttributeName_47,
						Messages.PBSJobAttributeDescription_47, true, Messages.PBSJobAttributeDefaultValue_47));
		attrMap.put(Messages.PBSJobAttributeName_48, // VARIABLE_LIST_ATTR
				new StringAttributeDefinition(Messages.PBSJobAttributeName_48, Messages.PBSJobAttributeName_48,
						Messages.PBSJobAttributeDescription_48, true, Messages.PBSJobAttributeDefaultValue_48));
	}

	/**
	 * This is a provisional method, and will be replaced by one in which the
	 * attributes are obtained from the proxy during model definition.
	 * 
	 * @return a mapping of the known (static) PBS Job Attributes.
	 */
	public static Map<String, IAttributeDefinition<?, ?, ?>> getAttributeDefinitionMap() {
		return attrMap;
	}
}