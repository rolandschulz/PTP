/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html Contributors:
 * IBM Corporation - Initial API and implementatio Albert L. Rossi (NCSA) -
 * Updated attributes (bug 310189) Updated attributes 04/30/2010 
 * Updated attributes 05/11/2010
 *******************************************************************************/
package org.eclipse.ptp.rm.pbs.core;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.ptp.core.attributes.BooleanAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;
import org.eclipse.ptp.rm.pbs.core.messages.Messages;

/**
 * Hard-coded mapping of known (static) PBS Job Attributes. These conform with
 * PBS's qsub, rather than representing all the attributes internal to the PBS
 * API.
 * 
 * @author arossi
 */
public class PBSJobAttributes {

	private static final Map<String, IAttributeDefinition<?, ?, ?>> attrMap;

	static {
		attrMap = new TreeMap<String, IAttributeDefinition<?, ?, ?>>();
		attrMap.put(Messages.PBSJobAttributeName_0, // ACCOUNT_NAME
				new StringAttributeDefinition(Messages.PBSJobAttributeName_0, Messages.PBSJobAttributeName_0,
						Messages.PBSJobAttributeDescription_0, true, Messages.PBSJobAttributeDefaultValue_0.trim()));
		attrMap.put(Messages.PBSJobAttributeName_1, // CHECKPOINT
				new StringAttributeDefinition(Messages.PBSJobAttributeName_1, Messages.PBSJobAttributeName_1,
						Messages.PBSJobAttributeDescription_1, true, Messages.PBSJobAttributeDefaultValue_1.trim()));
		attrMap.put(Messages.PBSJobAttributeName_2, // DEPEND
				new StringAttributeDefinition(Messages.PBSJobAttributeName_2, Messages.PBSJobAttributeName_2,
						Messages.PBSJobAttributeDescription_2, true, Messages.PBSJobAttributeDefaultValue_2.trim()));
		attrMap.put(Messages.PBSJobAttributeName_3, // DESTINATION
				new StringAttributeDefinition(Messages.PBSJobAttributeName_3, Messages.PBSJobAttributeName_3,
						Messages.PBSJobAttributeDescription_3, true, Messages.PBSJobAttributeDefaultValue_3.trim()));
		attrMap.put(Messages.PBSJobAttributeName_4, // DIRECTIVE
				new StringAttributeDefinition(Messages.PBSJobAttributeName_4, Messages.PBSJobAttributeName_4,
						Messages.PBSJobAttributeDescription_4, true, Messages.PBSJobAttributeDefaultValue_4.trim()));
		attrMap.put(Messages.PBSJobAttributeName_5, // EXPORT_ALL
				new BooleanAttributeDefinition(Messages.PBSJobAttributeName_5, Messages.PBSJobAttributeName_5,
						Messages.PBSJobAttributeDescription_5, true, new Boolean(Messages.PBSJobAttributeDefaultValue_5)));
		attrMap.put(Messages.PBSJobAttributeName_6, // ERROR_PATH
				new StringAttributeDefinition(Messages.PBSJobAttributeName_6, Messages.PBSJobAttributeName_6,
						Messages.PBSJobAttributeDescription_6, true, Messages.PBSJobAttributeDefaultValue_6.trim()));
		attrMap.put(Messages.PBSJobAttributeName_7, // EXECUTION_TIME
				new StringAttributeDefinition(Messages.PBSJobAttributeName_7, Messages.PBSJobAttributeName_7,
						Messages.PBSJobAttributeDescription_7, true, Messages.PBSJobAttributeDefaultValue_7.trim()));
		attrMap.put(Messages.PBSJobAttributeName_8, // GROUP_LIST
				new StringAttributeDefinition(Messages.PBSJobAttributeName_8, Messages.PBSJobAttributeName_8,
						Messages.PBSJobAttributeDescription_8, true, Messages.PBSJobAttributeDefaultValue_8.trim()));
		attrMap.put(Messages.PBSJobAttributeName_9, // HOLD_TYPES
				new StringAttributeDefinition(Messages.PBSJobAttributeName_9, Messages.PBSJobAttributeName_9,
						Messages.PBSJobAttributeDescription_9, true, Messages.PBSJobAttributeDefaultValue_9.trim()));
		attrMap.put(Messages.PBSJobAttributeName_10, // JOB_NAME
				new StringAttributeDefinition(Messages.PBSJobAttributeName_10, Messages.PBSJobAttributeName_10,
						Messages.PBSJobAttributeDescription_10, true, Messages.PBSJobAttributeDefaultValue_10.trim()));
		attrMap.put(Messages.PBSJobAttributeName_11, // JOIN_PATH
				new BooleanAttributeDefinition(Messages.PBSJobAttributeName_11, Messages.PBSJobAttributeName_11,
						Messages.PBSJobAttributeDescription_11, true, new Boolean(Messages.PBSJobAttributeDefaultValue_11)));
		attrMap.put(Messages.PBSJobAttributeName_12, // KEEP_FILES
				new StringAttributeDefinition(Messages.PBSJobAttributeName_12, Messages.PBSJobAttributeName_12,
						Messages.PBSJobAttributeDescription_12, true, Messages.PBSJobAttributeDefaultValue_12.trim()));
		attrMap.put(Messages.PBSJobAttributeName_13, // MAIL_POINTS
				new StringAttributeDefinition(Messages.PBSJobAttributeName_13, Messages.PBSJobAttributeName_13,
						Messages.PBSJobAttributeDescription_13, true, Messages.PBSJobAttributeDefaultValue_13.trim()));
		attrMap.put(Messages.PBSJobAttributeName_14, // MAIL_USERS
				new StringAttributeDefinition(Messages.PBSJobAttributeName_14, Messages.PBSJobAttributeName_14,
						Messages.PBSJobAttributeDescription_14, true, Messages.PBSJobAttributeDefaultValue_14.trim()));
		attrMap.put(Messages.PBSJobAttributeName_15, // OUTPUT_PATH
				new StringAttributeDefinition(Messages.PBSJobAttributeName_15, Messages.PBSJobAttributeName_15,
						Messages.PBSJobAttributeDescription_15, true, Messages.PBSJobAttributeDefaultValue_15.trim()));
		attrMap.put(Messages.PBSJobAttributeName_16, // PRIORITY
				new StringAttributeDefinition(Messages.PBSJobAttributeName_16, Messages.PBSJobAttributeName_16,
						Messages.PBSJobAttributeDescription_16, true, Messages.PBSJobAttributeDefaultValue_16.trim()));
		attrMap.put(Messages.PBSJobAttributeName_17, // RERUNNABLE
				new StringAttributeDefinition(Messages.PBSJobAttributeName_17, Messages.PBSJobAttributeName_17,
						Messages.PBSJobAttributeDescription_17, true, Messages.PBSJobAttributeDefaultValue_17.trim()));
		attrMap.put(Messages.PBSJobAttributeName_18, // ARCH
				new StringAttributeDefinition(Messages.PBSJobAttributeName_18, Messages.PBSJobAttributeName_18,
						Messages.PBSJobAttributeDescription_18, true, Messages.PBSJobAttributeDefaultValue_18.trim()));
		attrMap.put(Messages.PBSJobAttributeName_19, // CPUT
				new StringAttributeDefinition(Messages.PBSJobAttributeName_19, Messages.PBSJobAttributeName_19,
						Messages.PBSJobAttributeDescription_19, true, Messages.PBSJobAttributeDefaultValue_19.trim()));
		attrMap.put(Messages.PBSJobAttributeName_20, // FILE
				new StringAttributeDefinition(Messages.PBSJobAttributeName_20, Messages.PBSJobAttributeName_20,
						Messages.PBSJobAttributeDescription_20, true, Messages.PBSJobAttributeDefaultValue_20.trim()));
		attrMap.put(Messages.PBSJobAttributeName_21, // HOST
				new StringAttributeDefinition(Messages.PBSJobAttributeName_21, Messages.PBSJobAttributeName_21,
						Messages.PBSJobAttributeDescription_21, true, Messages.PBSJobAttributeDefaultValue_21.trim()));
		attrMap.put(Messages.PBSJobAttributeName_22, // MEM
				new StringAttributeDefinition(Messages.PBSJobAttributeName_22, Messages.PBSJobAttributeName_22,
						Messages.PBSJobAttributeDescription_22, true, Messages.PBSJobAttributeDefaultValue_22));
		attrMap.put(Messages.PBSJobAttributeName_23, // NCPUS
				new IntegerAttributeDefinition(Messages.PBSJobAttributeName_23, Messages.PBSJobAttributeName_23,
						Messages.PBSJobAttributeDescription_23, true, new Integer(Messages.PBSJobAttributeDefaultValue_23)));
		attrMap.put(Messages.PBSJobAttributeName_24, // NICE
				new IntegerAttributeDefinition(Messages.PBSJobAttributeName_24, Messages.PBSJobAttributeName_24,
						Messages.PBSJobAttributeDescription_24, true, new Integer(Messages.PBSJobAttributeDefaultValue_24)));
		attrMap.put(Messages.PBSJobAttributeName_25, // NODES
				new StringAttributeDefinition(Messages.PBSJobAttributeName_25, Messages.PBSJobAttributeName_25,
						Messages.PBSJobAttributeDescription_25, true, Messages.PBSJobAttributeDefaultValue_25.trim()));
		attrMap.put(Messages.PBSJobAttributeName_26, // OMPTHREADS
				new IntegerAttributeDefinition(Messages.PBSJobAttributeName_26, Messages.PBSJobAttributeName_26,
						Messages.PBSJobAttributeDescription_26, true, new Integer(Messages.PBSJobAttributeDefaultValue_26)));
		attrMap.put(Messages.PBSJobAttributeName_27, // PCPUT
				new StringAttributeDefinition(Messages.PBSJobAttributeName_27, Messages.PBSJobAttributeName_27,
						Messages.PBSJobAttributeDescription_27, true, Messages.PBSJobAttributeDefaultValue_27.trim()));
		attrMap.put(Messages.PBSJobAttributeName_28, // PMEM
				new StringAttributeDefinition(Messages.PBSJobAttributeName_28, Messages.PBSJobAttributeName_28,
						Messages.PBSJobAttributeDescription_28, true, Messages.PBSJobAttributeDefaultValue_28.trim()));
		attrMap.put(Messages.PBSJobAttributeName_29, // PVMEM
				new StringAttributeDefinition(Messages.PBSJobAttributeName_29, Messages.PBSJobAttributeName_29,
						Messages.PBSJobAttributeDescription_29, true, Messages.PBSJobAttributeDefaultValue_29.trim()));
		attrMap.put(Messages.PBSJobAttributeName_30, // VMEM
				new StringAttributeDefinition(Messages.PBSJobAttributeName_30, Messages.PBSJobAttributeName_30,
						Messages.PBSJobAttributeDescription_30, true, Messages.PBSJobAttributeDefaultValue_30.trim()));
		attrMap.put(Messages.PBSJobAttributeName_31, // WALLTIME
				new StringAttributeDefinition(Messages.PBSJobAttributeName_31, Messages.PBSJobAttributeName_31,
						Messages.PBSJobAttributeDescription_31, true, Messages.PBSJobAttributeDefaultValue_31));
		attrMap.put(Messages.PBSJobAttributeName_32, // SHELL_PATH_LIST
				new StringAttributeDefinition(Messages.PBSJobAttributeName_32, Messages.PBSJobAttributeName_32,
						Messages.PBSJobAttributeDescription_32, true, Messages.PBSJobAttributeDefaultValue_32.trim()));
		attrMap.put(Messages.PBSJobAttributeName_33, // STAGEIN
				new StringAttributeDefinition(Messages.PBSJobAttributeName_33, Messages.PBSJobAttributeName_33,
						Messages.PBSJobAttributeDescription_33, true, Messages.PBSJobAttributeDefaultValue_33.trim()));
		attrMap.put(Messages.PBSJobAttributeName_34, // STAGEOUT
				new StringAttributeDefinition(Messages.PBSJobAttributeName_34, Messages.PBSJobAttributeName_34,
						Messages.PBSJobAttributeDescription_34, true, Messages.PBSJobAttributeDefaultValue_34.trim()));
		attrMap.put(Messages.PBSJobAttributeName_35, // USER_LIST
				new StringAttributeDefinition(Messages.PBSJobAttributeName_35, Messages.PBSJobAttributeName_35,
						Messages.PBSJobAttributeDescription_35, true, Messages.PBSJobAttributeDefaultValue_35.trim()));
		attrMap.put(Messages.PBSJobAttributeName_36, // VARIABLE_LIST
				new StringAttributeDefinition(Messages.PBSJobAttributeName_36, Messages.PBSJobAttributeName_36,
						Messages.PBSJobAttributeDescription_36, true, Messages.PBSJobAttributeDefaultValue_36.trim()));
		// non-standard, added internally
		attrMap.put(Messages.PBSJobAttributeName_37, // MPIOPTIONS
				new StringAttributeDefinition(Messages.PBSJobAttributeName_37, Messages.PBSJobAttributeName_37,
						Messages.PBSJobAttributeDescription_37, true, Messages.PBSJobAttributeDefaultValue_37.trim()));
		// non-standard, added internally
		attrMap.put(Messages.PBSJobAttributeName_38, // SCRIPT
				new StringAttributeDefinition(Messages.PBSJobAttributeName_38, Messages.PBSJobAttributeName_38,
						Messages.PBSJobAttributeDescription_38, true, Messages.PBSJobAttributeDefaultValue_38.trim()));
		// non-standard, added internally
		attrMap.put(Messages.PBSJobAttributeName_39, // MPICOMMAND
				new StringAttributeDefinition(Messages.PBSJobAttributeName_39, Messages.PBSJobAttributeName_39,
						Messages.PBSJobAttributeDescription_39, true, Messages.PBSJobAttributeDefaultValue_39.trim()));
		// non-standard, added internally
		attrMap.put(Messages.PBSJobAttributeName_40, // PREPEND
				new StringAttributeDefinition(Messages.PBSJobAttributeName_40, Messages.PBSJobAttributeName_40,
						Messages.PBSJobAttributeDescription_40, true, Messages.PBSJobAttributeDefaultValue_40.trim()));
		// non-standard, added internally
		attrMap.put(Messages.PBSJobAttributeName_41, // POSTPEND
				new StringAttributeDefinition(Messages.PBSJobAttributeName_41, Messages.PBSJobAttributeName_41,
						Messages.PBSJobAttributeDescription_41, true, Messages.PBSJobAttributeDefaultValue_41.trim()));
	}

	/**
	 * This is a provisional method, and will be replaced by one in which the
	 * attributes are obtained from the proxy during model definition.
	 * 
	 * @return a mapping of the known (static) PBS Job Attributes.
	 * @since 4.0
	 */
	public static Map<String, IAttributeDefinition<?, ?, ?>> getAttributeDefinitionMap() {
		return attrMap;
	}
}